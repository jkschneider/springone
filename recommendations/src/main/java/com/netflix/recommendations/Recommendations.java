package com.netflix.recommendations;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerMapping;

import com.google.common.collect.Sets;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@SpringBootApplication
@EnableCircuitBreaker
@EnableEurekaClient
@EnableFeignClients
public class Recommendations {
    // only necessary when Feign is not there
//    @Bean public RestTemplate restTemplate(SpringClientFactory springClientFactory,
//                       LoadBalancerClient loadBalancerClient) {
//       return new RestTemplate(new RibbonClientHttpRequestFactory(springClientFactory, loadBalancerClient));
//    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Recommendations.class).web(true).run(args);
    }
}

@Data
class Movie {
    final String title;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Member {
    String user;
    Integer age;
}

@RestController
@RequestMapping("/api/recommendations")
class RecommendationsController {
    @Inject RecommendationsRepository repository;

    @RequestMapping("/{user}")
    Set<Movie> recommendations(@PathVariable String user) {
//    	String mapping = (String) RequestContextHolder.currentRequestAttributes().getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        return repository.findRecommendationsForUser(user);
    }

    @RequestMapping("/feign/{user}")
    Set<Movie> recommendationsWithFeign(@PathVariable String user) {
        return repository.findRecommendationsForUserWithFeign(user);
    }
}

@FeignClient("membership")
interface MembershipRepository {
    @RequestMapping(method = RequestMethod.GET, value = "/api/member/{user}")
    Member findMember(@PathVariable("user") String user);
}

@Named
class RecommendationsRepository {
    @Inject RestTemplate rest;
    @Inject MembershipRepository membershipRepository;

    Set<Movie> kidRecommendations = Sets.newHashSet(new Movie("lion king"), new Movie("frozen"));
    Set<Movie> adultRecommendations = Sets.newHashSet(new Movie("shawshank redemption"), new Movie("spring"));
    Set<Movie> familyRecommendations = Sets.newHashSet(new Movie("hook"), new Movie("the sandlot"));

    @HystrixCommand(fallbackMethod = "recommendationFallback", commandProperties={
//    		   	@HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE"),
    			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "100")})
    public Set<Movie> findRecommendationsForUser(String user) {
//       	String mapping = (String) RequestContextHolder.currentRequestAttributes().getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        Member member = rest.getForObject("http://membership/api/member/{user}", Member.class, user);
        return member.age < 17 ? kidRecommendations : adultRecommendations;
    }

    Set<Movie> findRecommendationsForUserWithFeign(String user) {
        Member member = membershipRepository.findMember(user);
        return member.age < 17 ? kidRecommendations : adultRecommendations;
    }

    /**
     * Should be safe for all audiences
     */
    Set<Movie> recommendationFallback(String user) {
        return familyRecommendations;
    }
}