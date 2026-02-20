package com.orkestra.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.orkestra")
public class OrkestraApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrkestraApplication.class, args);
    }
}
