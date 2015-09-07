package com.netflix.membership

import groovy.transform.Canonical
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.netflix.metrics.atlas.AtlasAutoConfiguration
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.metrics.atlas.AtlasExporter
import org.springframework.cloud.netflix.metrics.spectator.SpectatorAutoConfiguration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*

@SpringBootApplication
@EnableEurekaClient
//@EnableSpectator
//@EnableAtlas
@EnableScheduling
@ImportAutoConfiguration([SpectatorAutoConfiguration, AtlasAutoConfiguration])
class Membership {
    static void main(String[] args) {
        new SpringApplicationBuilder(Membership).web(true).run(args)
    }

    @Autowired
    AtlasExporter exporter

    @Scheduled(fixedRate = 5000L)
    void pushMetricsToAtlas() {
        exporter.export()
    }
}

@Canonical
class Member {
    String user
    Integer age
}

@RestController
@RequestMapping('/api/member')
class MembershipController {
    Map<String, Member> memberStore = [
        jschneider: new Member('jschneider', 10),
        twicksell: new Member('twicksell', 30)
    ]

    @RequestMapping(method = RequestMethod.POST)
    Member register(@RequestBody Member member) {
        memberStore[member.user] = member
        return member
    }

    @RequestMapping('/{user}')
    Member login(@PathVariable String user) {
        memberStore.get(user)
    }
}