package org.projectodd.restafari.stomp.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.Stomp;

/**
 * @author Bob McWhirter
 */
public class StompContentFrame extends StompFrame {

    public StompContentFrame(Stomp.Command command) {
        super( command );
    }

    public StompContentFrame(Stomp.Command command, Headers headers) {
        super( command, headers );
    }

    public StompContentFrame(FrameHeader header) {
        super( header );
    }

    public StompContentFrame(FrameHeader header, ByteBuf content) {
        super( header );
        this.content = content;
    }

    public StompContentFrame(FrameHeader header, String content) {
        super( header);
        this.content = Unpooled.copiedBuffer(content.getBytes());
    }

    public void content(ByteBuf content) {
        this.content = content;
    }

    public ByteBuf content() {
        return Unpooled.wrappedBuffer( this.content );
    }

    private ByteBuf content;
}

