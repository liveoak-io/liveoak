package io.liveoak.scripts;

import io.liveoak.common.codec.binary.DefaultBinaryResourceState;
import io.liveoak.spi.MediaType;
import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class JavaScriptResourceState extends DefaultBinaryResourceState {

    public JavaScriptResourceState(ByteBuf buffer) {
        super(buffer);
    }

    @Override
    public String getMimeType() {
        return MediaType.JAVASCRIPT.toString();
    }

}
