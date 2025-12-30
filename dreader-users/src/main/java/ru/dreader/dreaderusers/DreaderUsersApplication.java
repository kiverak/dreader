package ru.dreader.dreaderusers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"ru.dreader.dreaderusers.**"})
@EntityScan(basePackages = "entity")
@RefreshScope
public class DreaderUsersApplication {

    static void main(String[] args) {
        SpringApplication.run(DreaderUsersApplication.class, args);
    }

}
