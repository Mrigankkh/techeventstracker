package com.techevents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TechEventsApplication {
    public static void main(String[] args) {
        SpringApplication.run(TechEventsApplication.class, args);
    }
}
