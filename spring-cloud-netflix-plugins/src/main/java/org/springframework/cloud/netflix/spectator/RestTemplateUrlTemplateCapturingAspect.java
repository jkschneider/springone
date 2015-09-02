package org.springframework.cloud.netflix.spectator;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class RestTemplateUrlTemplateCapturingAspect {
    @Around("execution(* org.springframework.web.client.RestTemplate.*(String, ..))")
    void captureUrlTemplate(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            String urlTemplate = (String) joinPoint.getArgs()[0];
            RestTemplateUrlTemplateHolder.setRestTemplateUrlTemplate(urlTemplate);
            joinPoint.proceed();
        } finally {
            RestTemplateUrlTemplateHolder.clear();
        }
    }
}
