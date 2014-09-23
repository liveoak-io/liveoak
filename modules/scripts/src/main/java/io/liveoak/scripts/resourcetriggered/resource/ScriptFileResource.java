package io.liveoak.scripts.resourcetriggered.resource;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.BinaryContentSink;
import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.BinaryResourceState;
import io.liveoak.spi.state.LazyResourceState;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptFileResource implements BinaryResource {

    static final String ID = "script";

    protected ResourceScript parent;

    public ScriptFileResource(ResourceScript parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MediaType mediaType() {
        return MediaType.JAVASCRIPT;
    }

    @Override
    public long contentLength() {
        if (parent.getScript() != null && parent.getScript().getScriptBuffer() != null) {
            return parent.getScript().getScriptBuffer().readableBytes();
        } else {
            return 0;
        }
    }

    @Override
    public void readContent(RequestContext ctx, BinaryContentSink sink) throws Exception {
        if (parent.getScript() != null) {
            sink.accept(parent.getScript().getScriptBuffer());
        }
        sink.close();
    }

    @Override
    public void updateContent(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        ByteBuf content = null;

        if (state instanceof LazyResourceState) {
            content = ((LazyResourceState) state).contentAsByteBuf();
        } else if (state instanceof BinaryResourceState) {
            content = ((BinaryResourceState) state).getBuffer();
        }

        if (content != null) {
            parent.updateScript(content);
            responder.resourceUpdated(this);
            return;
        }

        responder.invalidRequest("The uploaded script must be a binary javascript file.");
    }

    @Override
    public boolean willProcessUpdate(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        return true;
    }
}
