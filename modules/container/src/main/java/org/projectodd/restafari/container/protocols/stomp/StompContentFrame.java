package org.projectodd.restafari.container.protocols.stomp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Bob McWhirter
 */
public class StompContentFrame extends StompFrame {

    public StompContentFrame(Command command) {
        super( command );
    }

    public StompContentFrame(Command command, Headers headers) {
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

    public void setContent(ByteBuf content) {
        this.content = content;
    }

    public ByteBuf getContent() {
        return Unpooled.wrappedBuffer( this.content );
    }

    private ByteBuf content;
}

