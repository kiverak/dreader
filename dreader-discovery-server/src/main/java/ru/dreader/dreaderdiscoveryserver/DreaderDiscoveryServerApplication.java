package ru.dreader.dreaderdiscoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DreaderDiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DreaderDiscoveryServerApplication.class, args);
    }

}
