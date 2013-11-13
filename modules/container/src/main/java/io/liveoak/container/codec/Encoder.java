package io.liveoak.container.codec;

import io.netty.buffer.ByteBuf;
import io.liveoak.spi.resource.async.Resource;

import java.util.Date;

/**
 * @author Bob McWhirter
 */
public interface Encoder extends AutoCloseable {

    void initialize(ByteBuf buffer) throws Exception;
    void close() throws Exception;

    void startResource(Resource resource) throws Exception;
    void endResource(Resource resource) throws Exception;

    void startProperty(String propertyName) throws  Exception;
    void endProperty(String propertyName) throws  Exception;

    void startMembers() throws  Exception;
    void endMembers() throws  Exception;

    void startList() throws Exception;
    void endList() throws Exception;

    void writeValue(String value) throws Exception;
    void writeValue(Integer value) throws Exception;
    void writeValue(Double value) throws Exception;
    void writeValue(Date value) throws Exception;
}
