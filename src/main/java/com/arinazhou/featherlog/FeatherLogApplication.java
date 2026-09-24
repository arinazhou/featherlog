package com.arinazhou.featherlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class FeatherLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeatherLogApplication.class, args);
    }
}
