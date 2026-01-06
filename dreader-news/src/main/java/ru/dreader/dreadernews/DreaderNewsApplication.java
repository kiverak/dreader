package ru.dreader.dreadernews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EntityScan(basePackages = {
        "ru.dreader.dreadernews.security",
        "ru.dreader.dreadernews.entity",
        "ru.dreader.mvc.exception"
})
public class DreaderNewsApplication {

    static void main(String[] args) {
        SpringApplication.run(DreaderNewsApplication.class, args);
    }

}
