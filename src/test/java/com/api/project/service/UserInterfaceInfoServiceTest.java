package com.api.project.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@SpringBootTest
public class UserInterfaceInfoServiceTest {


    @Resource
    private UserInterfaceInfoService UserInterfaceInfoService;
    @Test
    public void invokeCount() {
        boolean b= UserInterfaceInfoService.invokeCount(1L,1);
        Assertions.assertTrue(b);
    }
}