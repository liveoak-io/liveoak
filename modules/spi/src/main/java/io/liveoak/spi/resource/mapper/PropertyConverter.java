package io.liveoak.spi.resource.mapper;

/**
 * Provides ability to convert to/from values for a {@link Property}.
 *
 * @author Ken Finnigan
 */
public interface PropertyConverter<T> {

    Object toValue(T value) throws Exception;

    T createFrom(Object value) throws Exception;
}
