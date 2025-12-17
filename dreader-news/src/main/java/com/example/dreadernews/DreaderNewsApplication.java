package com.example.dreadernews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDiscoveryClient
public class DreaderNewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DreaderNewsApplication.class, args);
    }

}
