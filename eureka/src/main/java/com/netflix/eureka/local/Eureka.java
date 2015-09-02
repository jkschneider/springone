package com.netflix.eureka.local;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class Eureka {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Eureka.class).web(true).run(args);
    }
}
