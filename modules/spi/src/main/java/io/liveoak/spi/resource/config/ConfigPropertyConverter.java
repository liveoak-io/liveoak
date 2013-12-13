package io.liveoak.spi.resource.config;

/**
 * Provides ability to convert to/from config values for a {@link io.liveoak.spi.resource.config.ConfigProperty}.
 *
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public interface ConfigPropertyConverter<T> {

    Object toConfigValue(T value) throws Exception;

    T createFrom(Object configValue) throws Exception;
}
