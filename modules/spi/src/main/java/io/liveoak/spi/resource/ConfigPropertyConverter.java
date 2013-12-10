package io.liveoak.spi.resource;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public interface ConfigPropertyConverter<T> {
    T fromConfigValue(String value);

    String toConfigValue(T value);
}
