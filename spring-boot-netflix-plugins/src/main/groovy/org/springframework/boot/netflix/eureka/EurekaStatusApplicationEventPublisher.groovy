package org.springframework.boot.netflix.eureka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Scheduled;

import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.DiscoveryClient;

public class EurekaStatusApplicationEventPublisher implements ApplicationEventPublisherAware {

	private InstanceStatus currentStatus = InstanceStatus.UNKNOWN;
	private ApplicationEventPublisher publisher;
	
	@Autowired
	DiscoveryClient discoveryClient;
	
	@Scheduled(fixedDelay = 3000L)
	public void checkDiscovery() {
		
		InstanceStatus latestStatus = null;
		latestStatus = discoveryClient.getInstanceRemoteStatus();
		if(!latestStatus.equals(currentStatus))
		{
			this.currentStatus=latestStatus;
			publisher.publishEvent(new EurekaStatusChangedEvent(latestStatus));
		}		
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher=applicationEventPublisher;
	}
	
	public synchronized InstanceStatus getCachedInstancestatus(){
		return this.currentStatus;
	}
	
}