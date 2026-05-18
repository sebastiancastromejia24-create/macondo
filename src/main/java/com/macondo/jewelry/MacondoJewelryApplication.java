package com.macondo.jewelry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MacondoJewelryApplication {
    public static void main(String[] args) {
        SpringApplication.run(MacondoJewelryApplication.class, args);
    }
}
