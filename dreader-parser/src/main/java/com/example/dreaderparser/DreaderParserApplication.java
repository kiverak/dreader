package com.example.dreaderparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDiscoveryClient
public class DreaderParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(DreaderParserApplication.class, args);
    }

}
