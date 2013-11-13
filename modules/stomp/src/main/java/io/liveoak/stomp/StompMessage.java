package io.liveoak.stomp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;

import java.nio.charset.Charset;

/**
 * @author Bob McWhirter
 */
public interface StompMessage extends ByteBufHolder {

    String id();

    Headers headers();

    String destination();

    void destination(String destination);

    String contentType();

    void contentType(String contentType);

    String content(Charset charset);

    String utf8Content();

    void content(String content, Charset charset);

    void content(String content);

    ByteBuf content();

    void content(ByteBuf content);

    boolean isError();

    void ack() throws StompException;

    void nack() throws StompException;

    void ack(String transactionId) throws StompException;

    void nack(String transactionId) throws StompException;

    StompMessage duplicate();

    StompMessage retain();

}
