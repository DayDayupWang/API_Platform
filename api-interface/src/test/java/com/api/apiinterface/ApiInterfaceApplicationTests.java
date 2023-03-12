package com.api.apiinterface;
import com.api.apiclientsdk.model.User;

import com.api.apiclientsdk.client.ApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ApiInterfaceApplicationTests {

    @Resource
    private ApiClient apiClient;

    @Test
    void contextLoads() {
        String result = apiClient.getNameByGet("aka");
        User user = new User();
        user.setUsername("嵌糕");
        String usernameByPost = apiClient.getUsernameByPost(user);
        System.out.println(result);
        System.out.println(usernameByPost);
    }

}
