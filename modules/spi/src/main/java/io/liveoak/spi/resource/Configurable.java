package io.liveoak.spi.resource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines whether a {@link RootResource} wishes to specify its' configuration as fields
 * on itself with {@link ConfigProperty}, instead of a separate {@link ConfigResource} implementation.
 *
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configurable {
}
