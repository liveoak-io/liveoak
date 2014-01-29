package io.liveoak.spi.resource.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a configuration property on a {@link io.liveoak.spi.resource.RootResource} for reading and updating,
 * or multiple configuration properties, as method parameters, on a single method to be combined into a single
 * object on the {@link io.liveoak.spi.resource.RootResource}.
 *
 * If <code>value</code> is set to an empty string, then the name of the configuration property will default
 * to the field name or method parameter name.
 *
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigProperty {

    String value() default "";

    String msg() default "";

    Class<? extends ConfigPropertyConverter> converter() default ConfigPropertyConverter.class;
}
