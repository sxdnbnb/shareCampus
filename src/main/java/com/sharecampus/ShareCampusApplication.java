package com.sharecampus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.sharecampus.mapper")
@SpringBootApplication
public class ShareCampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShareCampusApplication.class, args);
    }

}