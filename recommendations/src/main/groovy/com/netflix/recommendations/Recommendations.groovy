package com.netflix.recommendations

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class Recommendations {
    static void main(String[] args) {
        new SpringApplicationBuilder(Recommendations).web(true).run(args)
    }
}
