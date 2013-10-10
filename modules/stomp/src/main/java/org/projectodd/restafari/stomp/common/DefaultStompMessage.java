package org.projectodd.restafari.stomp.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.StompException;
import org.projectodd.restafari.stomp.StompMessage;

import java.nio.charset.Charset;

/**
 * @author Bob McWhirter
 */
public class DefaultStompMessage implements StompMessage {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public DefaultStompMessage() {
        this( new HeadersImpl() );
    }

    public DefaultStompMessage(Headers headers) {
        this( headers, null, false );
    }

    public DefaultStompMessage(Headers headers, ByteBuf content) {
        this( headers, content, false );

    }

    public DefaultStompMessage(Headers headers, ByteBuf content, boolean error) {
        this.headers = headers;
        this.content = content;
        this.error = error;
    }

    @Override
    public String getId() {
        return this.headers.get( Headers.MESSAGE_ID );
    }

    @Override
    public Headers getHeaders() {
        return this.headers;
    }

    @Override
    public String getDestination() {
        return this.headers.get( Headers.DESTINATION );
    }

    @Override
    public void setDestination(String destination) {
        this.headers.put( Headers.DESTINATION, destination );
    }

    @Override
    public String getContentType() {
        return this.headers.get( Headers.CONTENT_TYPE );
    }

    @Override
    public void setContentType(String contentType) {
        this.headers.put( Headers.CONTENT_TYPE, contentType );
    }

    @Override
    public String getContentAsString() {
        return this.content.toString( UTF_8 );
    }

    @Override
    public void setContentAsString(String content) {
        this.content = Unpooled.copiedBuffer( content.toCharArray(), UTF_8 );
    }

    @Override
    public ByteBuf getContent() {
        return this.content;
    }

    @Override
    public void setContent(ByteBuf content) {
        this.content = content;
    }

    @Override
    public boolean isError() {
        return this.error;
    }

    @Override
    public void ack() throws StompException {
    }

    @Override
    public void nack() throws StompException {
    }

    @Override
    public void ack(String transactionId) throws StompException {
    }

    @Override
    public void nack(String transactionId) throws StompException {
    }

    private Headers headers;
    private ByteBuf content;
    private boolean error;
}
