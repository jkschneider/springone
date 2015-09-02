package org.springframework.boot.netflix.atlas

import java.lang.annotation.*

/**
 * Convenience annotation for clients to enable Atlas metric collection.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EnableAtlas {
}
