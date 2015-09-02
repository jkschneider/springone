package org.springframework.boot.netflix.eureka;

import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.EurekaClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

/**
 * Binds the Spring Boot health status to Eureka. If Boot indicates your application is DOWN,
 * that status will be communicated to Eureka.
 */
public class EurekaHealthNotifier implements HealthCheckHandler {
    private HealthIndicator healthIndicator;

    public EurekaHealthNotifier(HealthIndicator healthIndicator, EurekaClient eurekaClient) {
        this.healthIndicator = healthIndicator;
        eurekaClient.registerHealthCheck(this);
    }

    @Override
    public InstanceStatus getStatus(InstanceStatus currentStatus) {
        Health health = healthIndicator.health();
        if (Status.DOWN == health.getStatus()) {
            return InstanceInfo.InstanceStatus.DOWN;
        } else if (Status.OUT_OF_SERVICE == health.getStatus()) {
            return InstanceInfo.InstanceStatus.OUT_OF_SERVICE;
        } else if (Status.UP == health.getStatus()) {
            return InstanceInfo.InstanceStatus.UP;
        }
        return InstanceInfo.InstanceStatus.UNKNOWN;
    }
}
