package org.springframework.boot.netflix.atlas

import java.lang.annotation.*

/**
 * Annotation for clients to enable Atlas metrics publishing.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EnableAtlas {
}
