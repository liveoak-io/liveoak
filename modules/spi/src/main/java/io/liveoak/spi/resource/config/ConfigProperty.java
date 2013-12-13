package io.liveoak.spi.resource.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a configuration property on a {@link io.liveoak.spi.resource.RootResource} for reading and updating.
 * If <code>value</code> is set to an empty string, then the name of the configuration property will default
 * to the field name.
 *
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigProperty {

    String value() default "";

    String msg() default "";

    Class<? extends ConfigPropertyConverter> converter() default ConfigPropertyConverter.class;
}
