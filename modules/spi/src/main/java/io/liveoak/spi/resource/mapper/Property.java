package io.liveoak.spi.resource.mapper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a property on a {@link io.liveoak.spi.resource.RootResource} for reading and updating,
 * or multiple properties, as method parameters, on a single method to be combined into a single
 * object on the {@link io.liveoak.spi.resource.RootResource}.
 *
 * If <code>value</code> is set to an empty string, then the name of the property will default
 * to the field name or method parameter name.
 *
 * @author Ken Finnigan
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Property {

    String value() default "";

    String msg() default "";

    Class<? extends PropertyConverter> converter() default PropertyConverter.class;
}
