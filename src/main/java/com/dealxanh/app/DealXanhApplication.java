package com.dealxanh.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DealXanhApplication {

    public static void main(String[] args) {
        SpringApplication.run(DealXanhApplication.class, args);
    }

}
