package org.projectodd.restafari.container.responders;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.spi.resource.BlockingResource;
import org.projectodd.restafari.spi.resource.Resource;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Bob McWhirter
 */
public abstract class TraversingResponder extends BaseResponder {

    public TraversingResponder(Executor executor, Resource root, ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super(inReplyTo, ctx);
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

    protected void doRead(String next, Resource resource) {
        if (resource instanceof BlockingResource) {
            this.executor.execute(() -> {
                try {
                    resource.read(next, this);
                } catch (RuntimeException e) {
                    noSuchResource(next);
                }
            });
        } else {
            try {
                resource.read(next, this);
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

    private ResourcePath remainingPath;
    private Executor executor;
    private Resource currentResource;
}
