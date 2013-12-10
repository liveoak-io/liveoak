/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.protocols.http;

import java.nio.charset.Charset;
import java.util.List;

import io.liveoak.common.DefaultResourceRequest;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.state.LazyResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.util.ReferenceCountUtil;
import org.jboss.logging.Logger;

/**
 * This object performs a role similar to HttpObjectAggregator.
 * If DefaultResourceRequest is received, it means the request head has been parsed and converted, but not yet
 * the request body necessarily.
 *
 * We try to handle the body intelligently. First, automatic read is turned off, so that we control the flow of read events.
 * Second, we process body in a lazy fashion. DefaultResourceRequest is passed on before body is processed.
 * This makes it possible to perform security checks, and request parameter checks before processing the full body content.
 *
 * Up to some limit body content is copied to memory buffer. If size exceeds certain limit a disk cache is used.
 *
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class HttpRequestBodyHandler extends MessageToMessageDecoder<Object> {

    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String CONTENT_TYPE = "Content-Type";

    private static Logger log = Logger.getLogger(HttpRequestBodyHandler.class);

    private FileUpload fileUpload;
    private DefaultResourceRequest request;

    private boolean complete;
    private Invocation completion;

    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); //Use disk if size exceed


    /*
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {

        if (msg instanceof LastHttpContent) {

            ByteBuf content = ((HttpContent) msg).content();
            if (fileUpload != null) {
                // if it's a PUT or a POST
                fileUpload.addContent(content.retain(), true);

                // TODO - not sure this is ever necessary - defensive coding
                if (request.state() != null) {
                    ((LazyResourceState) request.state()).fileUpload(fileUpload);
                } else {
                    fileUpload.delete();
                }

                if (completion != null) {
                    // complete the request as body is now fully available
                    completion.run();
                    completion = null;
                } else {
                    // mark that body is fully available
                    complete = true;
                }
            } else if (content.readableBytes() > 0) {
                log.debug("on LastHttpContent: " + content.readableBytes() + " bytes discarded!");
            }

        } else if (msg instanceof HttpContent) {

            ByteBuf content = ((HttpContent) msg).content();
            if (fileUpload != null) {
                fileUpload.addContent(content.retain(), false);
            } else if (content.readableBytes() > 0) {
                log.debug("on HttpContent: " + content.readableBytes() + " bytes discarded!");
            }

            // only continue reading body if resource has declared interest
            if (completion != null) {
                ctx.pipeline().firstContext().read();
            }

        } else if (msg instanceof DefaultResourceRequest) {
            // beginning of a new request
            complete = false;
            if (fileUpload != null) {
                fileUpload.delete();
            }
            fileUpload = null;

            DefaultResourceRequest request = (DefaultResourceRequest) msg;
            if (request.requestType() != RequestType.CREATE && request.requestType() != RequestType.UPDATE) {
                // not POST or PUT
                out.add(request);
                ctx.pipeline().firstContext().read();
                return;
            }

            // use original HttpRequest to get to Content-Length, and Content-Type
            HttpRequest original = (HttpRequest) request.requestContext().requestAttributes().getAttribute("HTTP_REQUEST");

            // use last component of target URI as posted resource filename
            List<ResourcePath.Segment> segments = request.resourcePath().segments();
            String filename = segments.size() < 1 ? "unknown" : segments.get(segments.size()-1).name();
            factory.createAttribute(original, "filename", filename);

            String contentLength = original.headers().get(CONTENT_LENGTH);
            long clen = 0;
            if (contentLength != null) {
                factory.createAttribute(original, CONTENT_LENGTH, contentLength);
                try {
                    clen = Long.parseLong(contentLength);
                } catch (Exception ignored) {
                    log.debug("Invalid Content-Length received: " + contentLength);
                }
            }

            String contentType = original.headers().get(CONTENT_TYPE);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            factory.createAttribute(original, CONTENT_TYPE, contentType);
            fileUpload = factory.createFileUpload(original,
                    request.resourcePath().toString(), filename,
                    contentType, "binary", Charset.forName("utf-8"),
                    clen);

            // save request for later, so we can update it with fileUpload once body is fully available
            this.request = request;

            out.add(request);

        } else if (msg instanceof Invocation) {

            Invocation invocation = (Invocation) msg;
            if (complete) {
                // body is fully available we should continue processing the request
                invocation.run();
            } else {
                completion = invocation;
            }
        } else {
            // in any other case simply pass the message on as if we're not here
            out.add(ReferenceCountUtil.retain(msg));
        }
    }

    public static class Invocation {
        private Runnable invocation;

        public Invocation(Runnable invocation) {
            this.invocation = invocation;
        }

        public void run() {
            invocation.run();
        }
    }
}
