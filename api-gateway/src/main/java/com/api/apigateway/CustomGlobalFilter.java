package com.api.apigateway;

import com.api.apiclientsdk.utils.SignUtils;
import com.api.apicommon.model.entity.InterfaceInfo;
import com.api.apicommon.model.entity.User;
import com.api.apicommon.service.InnerInterfaceInfoService;
import com.api.apicommon.service.InnerUserInterfaceInfoService;
import com.api.apicommon.service.InnerUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 全局过滤器
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    //白名单，默认本地
    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    //接口信息HOST域名
    private static final String INTERFACE_HOST = "http://localhost:8090";

    private static final long FIVE_MINUTES = 5 * 60 * 1000L;
    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    //1.用户发送请求到API网关
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


        //2.请求日志
        ServerHttpRequest request = exchange.getRequest();
        String path = INTERFACE_HOST + request.getPath().value();
        String method = request.getMethod().toString();

        log.info("请求唯一标识：" + request.getId());
        log.info("请求路径：" + request.getPath().value());
        log.info("请求方法：" + request.getMethod());
        log.info("请求参数：" + request.getQueryParams());
        String sourceAddress = request.getLocalAddress().getHostString();
        log.info("请求来源地址hostString：" + request.getLocalAddress().getHostString());
        log.info("请求来源地址hostName：" + request.getLocalAddress().getHostName());
        ServerHttpResponse response = exchange.getResponse();
        //3.(黑白名单)
        //如果白名单里不存在这个地址那么就拒绝它
        if (!IP_WHITE_LIST.contains(sourceAddress)) {
            //设置状态码进行拦截
            response.setStatusCode(HttpStatus.FORBIDDEN);
            //返回结束的对象
            return response.setComplete();
        }
        //4.用户鉴权（判断aK、sk是否合法）
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");
        // todo 实际情况应该是去数据库中查是否已分配给用户
        User invokerUser = null;
        try {
            invokerUser = innerUserService.getInvokerUser(accessKey);
        } catch (Exception e) {
            log.error("getInvokerUser error", e);
        }
        if (invokerUser == null) {
            return handleNoAuth(response);
        }

        ////测试部分：假如accessKey不等于ak的话就禁止访问
        //if (!accessKey.equals("ak")) {
        //    return handleNoAuth(response);
        //}

        //判断时间
        if (Long.parseLong(nonce) > 10000L) {
            return handleNoAuth(response);
        }
        // 时间和当前时间不能超过 5 分钟
        long currentTime = System.currentTimeMillis() / 1000;

        if ((currentTime - Long.parseLong(timestamp)) >= FIVE_MINUTES) {
            return handleInvokeError(response);
        }
        // 实际情况中是从数据库中查出 secretKey
        String secretKey = invokerUser.getSecretKey();
        //此处直接使用管理员写死sk的方式通过验证进行测试
        String serverSign = SignUtils.genSign(body, secretKey);
        if (sign == null || !sign.equals(serverSign)) {
            return handleNoAuth(response);
        }


        //5.请求的模拟接口是否存在
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
        } catch (Exception e) {
            log.error("getInterfaceInfo error", e);
        }
        if (interfaceInfo == null) {
            return handleInvokeError(response);
        }

        //  是否有调用次数
        if (!innerUserInterfaceInfoService.hasInvokeNum(invokerUser.getId(), interfaceInfo.getId())) {
            return handleInvokeError(response);
        }
        //6.请求转发，调用模拟接口
        //7.响应日志
        return handleResponse(exchange, chain, interfaceInfo.getId(), invokerUser.getId());

    }

    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * 处理响应
     * 预期是等模拟接口调用完成，才记录响应日志、统计调用次数。
     * 问题是： chain.fitter 方法立刻返回了，直到 filter 过滤器 return 后才调用了模拟接口。
     * 原因是：chain.filter 是个异步操作
     * 解決方案：利用response 装饰者，增强原有 response 的处理能力
     *
     * @param exchange
     * @param chain
     * @return
     */
    private Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
        try {
            // 从交换机拿到原始response
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓冲区工厂 拿到缓存数据
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到状态码
            HttpStatus statusCode = originalResponse.getStatusCode();

            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        // 对象是响应式的
                        if (body instanceof Flux) {
                            // 我们拿到真正的body
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里面写数据
                            // 拼接字符串
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                // 7. 调用成功，接口调用次数+1
                                try {
                                    innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                } catch (Exception e) {
                                    log.error("invokeCount error", e);
                                }
                                // data从这个content中读取
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);// 释放掉内存
                                // 6.构建日志
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                String data = new String(content, StandardCharsets.UTF_8);// data
                                //顺便让我在控制台看一下
                                log.info("响应结果在这里：" + data);
                                rspArgs.add(data);
                                log.info("<--- status:{} data:{}"// data
                                        , rspArgs.toArray());// log.info("<-- {} {}", originalResponse.getStatusCode(), data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            // 8.调用失败返回错误状态码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange);// 降级处理返回数据
        } catch (Exception e) {
            log.error("gateway log exception.\n" + e);
            return chain.filter(exchange);
        }

    }

    //无权限处理器
    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }
    //请求错误处理器
    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}


