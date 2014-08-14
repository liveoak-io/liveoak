package io.liveoak.scripts.resourcetriggered.resource;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.BinaryContentSink;
import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;

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
        return parent.getScript().getScriptBuffer().readableBytes();
    }

    @Override
    public void readContent(RequestContext ctx, BinaryContentSink sink) throws Exception {
        sink.accept(parent.getScript().getScriptBuffer());
        sink.close();
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        parent.deleteMember(ctx, this.id(), responder);
    }
}
