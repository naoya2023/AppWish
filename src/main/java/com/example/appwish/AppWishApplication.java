package com.example.appwish;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@ComponentScan(basePackages = "com.example.appwish")
@EnableJpaRepositories(basePackages = "com.example.appwish.repository")
@EntityScan(basePackages ="com.example.appwish.model")

@SpringBootApplication
public class AppWishApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppWishApplication.class, args);
    }
}