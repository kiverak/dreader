package ru.dreader.dreadergateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DreaderGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DreaderGatewayApplication.class, args);
    }

}
