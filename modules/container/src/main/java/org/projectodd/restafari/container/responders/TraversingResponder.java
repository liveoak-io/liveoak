package org.projectodd.restafari.container.responders;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.spi.Resource;

/**
 * @author Bob McWhirter
 */
public abstract class TraversingResponder extends BaseResponder {

    public TraversingResponder(ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super(inReplyTo, ctx);
        this.remainingPath = inReplyTo.resourcePath().subPath();
    }

    @Override
    public void resourceRead(Resource resource) {
        if ( this.remainingPath.isEmpty() ) {
            perform( resource );
        } else {
            String next = this.remainingPath.head();
            this.remainingPath = this.remainingPath.subPath();
            resource.read(next, this);
        }
    }

    protected boolean isSeekingTail() {
        return this.remainingPath.isEmpty();
    }

    protected abstract void perform(Resource resource);

    private ResourcePath remainingPath;
}
