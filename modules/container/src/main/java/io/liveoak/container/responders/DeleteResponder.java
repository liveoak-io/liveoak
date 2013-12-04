/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.responders;

import io.liveoak.container.DefaultContainer;
import io.liveoak.container.ResourceRequest;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public class DeleteResponder extends TraversingResponder {

    public DeleteResponder(Executor executor, DefaultContainer container, ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super(executor, container, inReplyTo, ctx);
    }

    @Override
    public void perform(Resource resource) {
        if (resource instanceof RootResource && resource.parent() == null ) {
            container().unregisterResource((RootResource) resource);
            createBaseResponder().resourceDeleted( resource );
        } else {
            resource.delete(inReplyTo().requestContext(), createBaseResponder());
        }
    }

}
