package ru.dreader.dreaderusers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DreaderUsersApplication {

    static void main(String[] args) {
        SpringApplication.run(DreaderUsersApplication.class, args);
    }

}
