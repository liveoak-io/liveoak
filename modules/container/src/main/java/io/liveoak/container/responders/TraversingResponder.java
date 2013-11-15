/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.responders;

import io.netty.channel.ChannelHandlerContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.container.ResourceRequest;
import io.liveoak.container.aspects.ResourceAspectManager;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.Resource;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public abstract class TraversingResponder extends BaseResponder {

    public TraversingResponder(ResourceAspectManager aspectManager, Executor executor, Resource root, ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super(inReplyTo, ctx);
        this.aspectManager = aspectManager;
        this.executor = executor;
        this.currentResource = root;
        this.remainingPath = inReplyTo.resourcePath().subPath();
    }

    @Override
    public void resourceRead(Resource resource) {
        this.currentResource = resource;
        if (this.remainingPath.isEmpty()) {
            doPerform(resource);
        } else {
            String next = this.remainingPath.head();
            this.remainingPath = this.remainingPath.subPath();
            doRead(next, resource);
        }
    }

    protected boolean isSeekingTail() {
        return this.remainingPath.isEmpty();
    }

    public void doRead(String next, Resource resource) {

        if (resource instanceof BlockingResource) {
            this.executor.execute(() -> {
                try {
                    resource.readMember(TraversingResponder.this.inReplyTo().requestContext(), next, this);
                } catch (RuntimeException e) {
                    noSuchResource(next);
                }
            });
        } else {
            try {
                resource.readMember(TraversingResponder.this.inReplyTo().requestContext(), next, this);
            } catch (RuntimeException e) {
                noSuchResource( next );
            }
        }
    }

    protected void doPerform(Resource resource) {
        if (resource instanceof BlockingResource) {
            this.executor.execute(() -> {
                perform(resource);
            });
        } else {
            perform(resource);
        }
    }

    @Override
    public void noSuchResource(String id) {
        if (this.remainingPath.segments().isEmpty()) {
            if (currentResource != null) {
                int lastDotLoc = id.lastIndexOf('.');
                if (lastDotLoc >= 0) {
                    String idWithoutExtension = id.substring(0, lastDotLoc);
                    doRead(idWithoutExtension, currentResource);
                    return;
                }
            }
        }

        super.noSuchResource(id);
    }

    protected Resource currentResource() {
        return this.currentResource;
    }

    protected abstract void perform(Resource resource);

    private ResourceAspectManager aspectManager;

    private ResourcePath remainingPath;
    private Executor executor;
    private Resource currentResource;
}
