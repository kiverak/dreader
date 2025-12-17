package com.example.dreaderparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DreaderParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(DreaderParserApplication.class, args);
    }

}
