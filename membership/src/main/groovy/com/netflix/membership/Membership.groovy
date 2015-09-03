package com.netflix.membership

import groovy.transform.Canonical
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.netflix.atlas.AtlasAutoConfiguration
import org.springframework.cloud.netflix.atlas.EnableAtlas
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.spectator.EnableSpectator
import org.springframework.cloud.netflix.spectator.SpectatorAutoConfiguration
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@EnableEurekaClient
//@EnableSpectator
//@EnableAtlas
@ImportAutoConfiguration([SpectatorAutoConfiguration, AtlasAutoConfiguration])
class Membership {
    static void main(String[] args) {
        new SpringApplicationBuilder(Membership).web(true).run(args)
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