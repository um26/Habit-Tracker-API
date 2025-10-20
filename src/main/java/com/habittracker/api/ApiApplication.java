package com.habittracker.api;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class ApiApplication {

    @PostConstruct
    public void init(){
        // Set the default timezone for the entire application to India Standard Time
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}