package org.projectodd.restafari.container.protocols.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.container.responses.CollectionResponse;
import org.projectodd.restafari.container.responses.ResourceResponse;

import java.util.List;

public class HttpCollectionResponseEncoder extends MessageToMessageEncoder<CollectionResponse> {

    public HttpCollectionResponseEncoder(ResourceCodecManager codecManager) {
        this.codecManager = codecManager;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, CollectionResponse msg, List<Object> out) throws Exception {
        HttpResponseStatus status = HttpResponseStatus.OK;
        switch (msg.responseType()) {
            case READ:
                status = HttpResponseStatus.OK;
                break;
        }

        ResourceCodec codec = this.codecManager.getResourceCodec( msg.mimeType() );
        ByteBuf encoded = codec.encode(msg.resources());

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, encoded );
        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, encoded.readableBytes());
        response.headers().add(HttpHeaders.Names.CONTENT_TYPE, msg.mimeType() );
        out.add(response);
    }

    private ResourceCodecManager codecManager;
}
