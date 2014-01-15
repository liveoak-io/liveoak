/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec;

import io.liveoak.common.codec.driver.EncodingDriver;
import io.liveoak.common.codec.driver.RootEncodingDriver;
import io.liveoak.common.codec.driver.StateEncodingDriver;
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

    public ResourceCodec(Class<? extends StateEncoder> encoderClass, ResourceDecoder decoder) {
        this.encoderClass = encoderClass;
        this.decoder = decoder;
    }

    public boolean hasEncoder() {
        return this.encoderClass != null;
    }

    public boolean hasDecoder() {
        return this.decoder != null;
    }

    public ByteBuf encode(RequestContext ctx, ResourceState resourceState) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        StateEncoder encoder = this.encoderClass.newInstance();
        encoder.initialize(buffer);
        StateEncodingDriver driver = new StateEncodingDriver(ctx,encoder, resourceState );
        driver.encode();
        driver.close();
        return buffer;
    }

    public ResourceState decode(ByteBuf resource) throws Exception {
        return this.decoder.decode(resource);
    }

    private final Class<? extends StateEncoder> encoderClass;
    private final ResourceDecoder decoder;

}
