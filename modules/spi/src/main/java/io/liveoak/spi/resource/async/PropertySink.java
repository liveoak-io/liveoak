package io.liveoak.spi.resource.async;

/**
 * @author Bob McWhirter
 */
public interface PropertySink {

    void accept(String name, Object value);
    void close();

}
