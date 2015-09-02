package org.springframework.cloud.netflix.eureka;

import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.DiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Scheduled;

public class EurekaStatusApplicationEventPublisher implements ApplicationEventPublisherAware {
    private InstanceStatus currentStatus = InstanceStatus.UNKNOWN;
    private ApplicationEventPublisher publisher;

    @Autowired
    DiscoveryClient discoveryClient;

    @Scheduled(fixedDelay = 3000L)
    public void checkDiscovery() {
        InstanceStatus latestStatus = discoveryClient.getInstanceRemoteStatus();
        if(!latestStatus.equals(currentStatus)) {
            this.currentStatus = latestStatus;
            publisher.publishEvent(new EurekaStatusChangedEvent(latestStatus));
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}