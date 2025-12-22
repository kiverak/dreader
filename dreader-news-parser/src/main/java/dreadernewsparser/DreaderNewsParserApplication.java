package dreadernewsparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DreaderNewsParserApplication {

    static void main(String[] args) {
        SpringApplication.run(DreaderNewsParserApplication.class, args);
    }

}
