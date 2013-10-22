package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;

/**
 * @author Bob McWhirter
 */
public interface ResourceEncoder<T> {

    T createAttachment(ByteBuf output) throws Exception;

    void encode(EncodingContext<T> context) throws Exception;
}
