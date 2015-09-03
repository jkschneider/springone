package com.netflix.recommendations

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty
import groovy.transform.Canonical
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker
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
import javax.inject.Named

@SpringBootApplication
@EnableCircuitBreaker
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

@Canonical
class Movie {
    String title
}

@RestController
@RequestMapping('/api/recommendations')
class RecommendationsController {
    @Inject RecommendationsRepository repository

    @RequestMapping('/{user}')
    Set<Movie> recommendations(@PathVariable String user) {
        repository.getRecommendationsForUser(user)
    }
}

@Named
class RecommendationsRepository {
    @Inject RestTemplate rest

    Set<Movie> kidRecommendations = [new Movie('lion king'), new Movie('frozen')]
    Set<Movie> adultRecommendations = [new Movie('shawshank redemption'), new Movie('spring')]
    Set<Movie> familyRecommendations = [new Movie('hook'), new Movie('the sandlot')]

    @HystrixCommand(fallbackMethod = 'recommendationFallback')
    Set<Movie> getRecommendationsForUser(String user) {
        def member = rest.getForObject("http://membership/api/member/{user}", Map.class, user)
        member.age < 17 ? kidRecommendations : adultRecommendations
    }

    /**
     * Should be safe for all audiences
     */
    Set<Movie> recommendationFallback(String user) { familyRecommendations }
}