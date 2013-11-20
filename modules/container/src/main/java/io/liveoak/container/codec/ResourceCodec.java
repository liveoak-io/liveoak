/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec;

import io.liveoak.container.DefaultContainer;
import io.liveoak.container.codec.driver.EncodingDriver;
import io.liveoak.container.codec.driver.RootEncodingDriver;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.concurrent.CompletableFuture;

/**
 * @author Bob McWhirter
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class ResourceCodec {

    public ResourceCodec( DefaultContainer container, Class<? extends Encoder> encoderClass, ResourceDecoder decoder ) {
        this.container = container;
        this.encoderClass = encoderClass;
        this.decoder = decoder;
    }

    public boolean hasEncoder() {
        return this.encoderClass != null;
    }

    public boolean hasDecoder() {
        return this.decoder != null;
    }

    public ByteBuf encode( RequestContext ctx, Resource resource ) throws Exception {
        CompletableFuture<ByteBuf> future = new CompletableFuture<>();
        newEncodingDriver( ctx, resource, future ).encode();
        ByteBuf result = future.get();
        return result;
    }

    public ResourceState decode( ByteBuf resource ) throws Exception {
        return this.decoder.decode( resource );
    }

    protected EncodingDriver newEncodingDriver( RequestContext ctx, Resource resource, CompletableFuture<ByteBuf> future ) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        Encoder encoder = this.encoderClass.newInstance();
        encoder.initialize( buffer );
        RootEncodingDriver driver = new RootEncodingDriver( ctx, encoder, resource, () -> {
            future.complete( buffer );
        } );
        return driver;
    }


    private final DefaultContainer container;
    private final Class<? extends Encoder> encoderClass;
    private final ResourceDecoder decoder;

}
