# 嵌糕API接口平台 

王艺澔



# 一、 项目介绍

- 嵌糕API接口平台是一个基于 React + Spring Boot + Dubbo + Gateway 的API接口开放调用平台。可以帮助企业、个人统一开放接口，减少沟通成本，**避免重复造轮子**，为业务高效赋能。
- 主要功能：

  - SDK快速调用接口
  - API接入
  - 防止攻击（黑白名单） 
  - 不能随便调用（限制、开通） 
  - 统计调用次数 
  - 计费 
  - 流量保护

# 二、 开发思路

### 2.1 技术选型

#### 前端

1. React 18
2. Ant Design Pro 5.x 脚手架
3. Ant Design & Procomponents 组件库
4. Umi 4 前端框架
5. OpenAPI 前端代码生成

#### 后端

后端通过访问 localhost:7529/api/doc.html 就能在线调试接口，不需要前端配合

1. Java Spring Boot
2. MySQL 数据库
3. MyBatis-Plus 及 MyBatis X 自动生成
4. API 签名认证（Http 调用）
5. Spring Boot Starter（SDK 开发）
6. Dubbo 分布式（RPC、Nacos）
7. Swagger + Knife4j 接口文档生成
8. Spring Cloud Gateway 微服务网关
9. Hutool、Apache Common Utils、Gson 等工具库



### 2.2 功能架构

#### 项目分工

● api-frontend：8000端口，前端向用户展示的界面，与后端通过OpenAPI协调参数和端口
● api-backend：7529端口，后端接口管理（上传、下线、用户登录）后端接口文档位于http://localhost:7529/api/doc.html
● api-gateway：8090端口，网关
● api-interface：8123端口，接收经过网关验证的请求，提供各种接口服务
● api-client-sdk：客户端SDK，无端口，用于后端人员快速调试，发送请求到8090端口，由网关进行转发到后端的api-interface
● api-common：封装了一些公共的类和接口，用Maven进行管理放置在本地仓库中，并被其他项目引用依赖，实现了功能的解耦



#### 架构图

![image-20230313151901701](https://typingcat-picbed.oss-cn-hangzhou.aliyuncs.com/img/202303131519773.png)

### 2.3. 使用者角色

- 网络管理员：用系统给定的账号进行登录，可以对接口进行管理，并通过客户端 SDK 轻松调用接口。
- 用户：登录移动端应用，可以浏览菜品、添加购物车、设置地址、在线下单等。





# 三、 产品成品展示

### 登录界面

![image-20230313145059260](https://typingcat-picbed.oss-cn-hangzhou.aliyuncs.com/img/202303131450331.png)



### 接口信息主页

![image-20230313144345544](https://typingcat-picbed.oss-cn-hangzhou.aliyuncs.com/img/202303131443597.png)

### 接口管理界面

![image-20230313144304818](https://typingcat-picbed.oss-cn-hangzhou.aliyuncs.com/img/202303131443933.png)

### 接口分析界面

![image-20230313143243883](https://typingcat-picbed.oss-cn-hangzhou.aliyuncs.com/img/202303131432025.png)

### 接口调用界面

![image-20230313145326221](https://typingcat-picbed.oss-cn-hangzhou.aliyuncs.com/img/202303131453285.png)



### 后端接口文档界面

![image-20230313145506742](https://typingcat-picbed.oss-cn-hangzhou.aliyuncs.com/img/202303131455825.png)
