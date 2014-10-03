package io.liveoak.mongo.gridfs.util;

import java.util.Map;
import java.util.function.BiFunction;

import io.liveoak.spi.resource.async.PropertySink;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MapPropertySink implements PropertySink {

    private Map<String, Object> properties;

    public MapPropertySink(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public void accept(String name, Object value) {
        properties.put(name, value);
    }

    @Override
    public void error(Throwable e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void complete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceConfig(BiFunction<String[], Object, Object> function) {
        throw new UnsupportedOperationException();
    }
}
