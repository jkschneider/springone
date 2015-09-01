package org.springframework.boot.netflix.eureka;

import org.springframework.context.ApplicationEvent;

import com.netflix.appinfo.InstanceInfo.InstanceStatus;

public class EurekaStatusChangedEvent extends ApplicationEvent {

	private InstanceStatus status;
	
	public EurekaStatusChangedEvent(InstanceStatus status) {
		super(status);
		this.status=status;
	}
	
	public InstanceStatus getStatus()
	{
		return this.status;
	}	
}
