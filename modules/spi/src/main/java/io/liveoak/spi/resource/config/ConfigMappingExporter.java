package io.liveoak.spi.resource.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates which methods on a {@link io.liveoak.spi.resource.RootResource} are used to convert an object into its
 * constituent configuration value parts. If <code>value</code> is not set, the default will be the method name to
 * which the annotation applies.
 *
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigMappingExporter {
    String value() default "";
}
