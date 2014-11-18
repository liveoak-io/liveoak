package io.liveoak.spi.resource.mapper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates which method on a {@link io.liveoak.spi.resource.RootResource} is invoked to convert an object into its
 * constituent configuration value parts.
 *
 * @author Ken Finnigan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MappingExporter {
}
