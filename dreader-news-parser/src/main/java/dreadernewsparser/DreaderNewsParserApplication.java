package dreadernewsparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
public class DreaderNewsParserApplication {

    static void main(String[] args) {
        SpringApplication.run(DreaderNewsParserApplication.class, args);
    }

}
