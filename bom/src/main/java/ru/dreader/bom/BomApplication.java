package ru.dreader.bom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BomApplication {

    public static void main(String[] args) {
        SpringApplication.run(BomApplication.class, args);
    }

}
