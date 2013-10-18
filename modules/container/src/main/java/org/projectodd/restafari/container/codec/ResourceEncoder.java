package org.projectodd.restafari.container.codec;

import java.io.IOException;

import org.projectodd.restafari.spi.resource.Resource;

public interface ResourceEncoder<T> {
    void encode(Resource resource, EncodingDriver<T> driver) throws IOException;
    T createEncodingContext();
}
