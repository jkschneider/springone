package org.springframework.cloud.netflix.eureka;

import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import org.springframework.context.ApplicationEvent;

public class EurekaStatusChangedEvent extends ApplicationEvent {
    private InstanceStatus status;

    public EurekaStatusChangedEvent(InstanceStatus status) {
        super(status);
        this.status = status;
    }

    public InstanceStatus getStatus() {
        return this.status;
    }
}
