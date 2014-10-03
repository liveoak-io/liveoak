package io.liveoak.mongo.gridfs.util;

import java.util.function.BiFunction;

import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class ResourceStatePropertySink implements PropertySink {

    private ResourceState state;

    public ResourceStatePropertySink(ResourceState state) {
        this.state = state;
    }

    @Override
    public void accept(String name, Object value) {
        state.putProperty(name, value);
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
