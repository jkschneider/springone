package com.netflix.recommendations

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.ribbon.RibbonClientHttpRequestFactory
import org.springframework.cloud.netflix.ribbon.SpringClientFactory
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

import javax.inject.Inject

@SpringBootApplication
@EnableEurekaClient
class Recommendations {
    @Bean restTemplate(SpringClientFactory springClientFactory,
                       LoadBalancerClient loadBalancerClient) {
       new RestTemplate(new RibbonClientHttpRequestFactory(springClientFactory, loadBalancerClient))
    }

    static void main(String[] args) {
        new SpringApplicationBuilder(Recommendations).web(true).run(args)
    }
}

@RestController
@RequestMapping('/api/recommendations')
class RecommendationsController {
    @Inject RestTemplate rest

    @RequestMapping('/{user}')
    List<String> recommendations(@PathVariable String user) {
        def member = rest.getForObject("http://membership/api/member/{user}", Map.class, user)
        if(member)
            ['old yeller', 'lassie']
        else
            []
    }
}