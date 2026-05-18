package com.livestock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.livestock.mapper")
public class LivestockApplication {
    public static void main(String[] args) {
        SpringApplication.run(LivestockApplication.class, args);
    }
}
