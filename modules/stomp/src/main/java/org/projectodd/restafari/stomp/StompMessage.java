package org.projectodd.restafari.stomp;

import io.netty.buffer.ByteBuf;

/**
 * @author Bob McWhirter
 */
public interface StompMessage {

        String getId();
        Headers getHeaders();

        String getDestination();
        void setDestination(String destination);

        String getContentType();
        void setContentType(String contentType);

        String getContentAsString();
        void setContentAsString(String content);

        ByteBuf getContent();
        void setContent(ByteBuf content);

        boolean isError();

        void ack() throws StompException;
        void nack() throws StompException;

        void ack(String transactionId) throws StompException;
        void nack(String transactionId) throws StompException;

        StompMessage duplicate();

}
