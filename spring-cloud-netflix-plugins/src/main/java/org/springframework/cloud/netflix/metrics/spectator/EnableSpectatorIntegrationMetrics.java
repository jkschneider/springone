package org.springframework.cloud.netflix.metrics.spectator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable Spectator metrics collection for Spring Integration.
 * Requires Spring Integration 4.2.0 or higher.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EnableSpectatorIntegrationMetrics {
}
