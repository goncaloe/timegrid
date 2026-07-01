package com.timegrid;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TimeGridApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeGridApplication.class, args);
    }

}
