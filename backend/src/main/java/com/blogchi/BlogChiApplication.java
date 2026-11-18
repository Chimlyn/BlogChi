package com.blogchi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.blogchi.mapper")
public class BlogChiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogChiApplication.class, args);
    }
}
