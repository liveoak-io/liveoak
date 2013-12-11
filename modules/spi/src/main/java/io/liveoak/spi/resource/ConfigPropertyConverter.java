package io.liveoak.spi.resource;

/**
 * Provides ability to convert to/from JSON config values for a {@link ConfigProperty}.
 *
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public interface ConfigPropertyConverter<T> {

    T fromConfig(Object value) throws Exception;

    Object toConfig(T value) throws Exception;
}
