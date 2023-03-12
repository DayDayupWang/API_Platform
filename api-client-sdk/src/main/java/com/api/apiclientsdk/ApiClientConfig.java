package com.api.apiclientsdk;


import com.api.apiclientsdk.client.ApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties("api.client")
@ComponentScan
public class ApiClientConfig {

    private String accessKey;
    private String secretKey;

    @Bean
    public ApiClient apiClient(){
        //自动装配，通过ak，sk生成客户端
      return new ApiClient(accessKey,secretKey);
    }

}
