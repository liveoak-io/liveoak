package org.projectodd.restafari.stomp.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.StompException;
import org.projectodd.restafari.stomp.StompMessage;

import java.nio.charset.Charset;

/**
 * @author Bob McWhirter
 */
public class DefaultStompMessage implements ByteBufHolder, StompMessage {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public DefaultStompMessage() {
        this(new HeadersImpl());
    }

    public DefaultStompMessage(Headers headers) {
        this(headers, null, false);
    }

    public DefaultStompMessage(Headers headers, ByteBuf content) {
        this(headers, content, false);

    }

    public DefaultStompMessage(Headers headers, ByteBuf content, boolean error) {
        this.headers = headers;
        if (content != null) {
            this.content = content.duplicate().retain();
        }
        this.error = error;
    }

    public DefaultStompMessage(boolean error) {
        this.error = error;
    }

    @Override
    public String id() {
        return this.headers.get(Headers.MESSAGE_ID);
    }

    @Override
    public Headers headers() {
        return this.headers;
    }

    @Override
    public String destination() {
        return this.headers.get(Headers.DESTINATION);
    }

    @Override
    public void destination(String destination) {
        this.headers.put(Headers.DESTINATION, destination);
    }

    @Override
    public String contentType() {
        return this.headers.get(Headers.CONTENT_TYPE);
    }

    @Override
    public void contentType(String contentType) {
        this.headers.put(Headers.CONTENT_TYPE, contentType);
    }

    @Override
    public String content(Charset charset) {
        return this.content.toString(charset);
    }

    @Override
    public String utf8Content() {
        return content(UTF_8);
    }

    @Override
    public void content(String content, Charset charset) {
        if (this.content != null) {
            this.content().release();
        }
        this.content = Unpooled.copiedBuffer(content.toCharArray(), charset);
    }

    public void content(String content) {
        content(content, UTF_8);
    }

    @Override
    public ByteBuf content() {
        return this.content;
    }

    @Override
    public void content(ByteBuf content) {
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

    public StompMessage duplicate() {
        return new DefaultStompMessage(this.headers, this.content.duplicate().retain(), this.error);
    }

    @Override
    public ByteBufHolder copy() {
        return new DefaultStompMessage(this.headers, this.content.duplicate().retain(), this.error);
    }

    @Override
    public int refCnt() {
        return this.content.refCnt();
    }

    @Override
    public ByteBufHolder retain() {
        this.content.retain();
        return this;
    }

    @Override
    public ByteBufHolder retain(int increment) {
        this.content.retain(increment);
        return this;
    }

    @Override
    public boolean release() {
        return this.content.release();
    }

    @Override
    public boolean release(int decrement) {
        return this.content.release(decrement);
    }

    private Headers headers;
    private ByteBuf content;
    private boolean error;
}
