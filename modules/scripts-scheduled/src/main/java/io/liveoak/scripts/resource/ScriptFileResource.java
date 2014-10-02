package io.liveoak.scripts.resource;

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

    public static final String ID = "script";

    protected ScriptResource parent;

    public ScriptFileResource(ScriptResource parent) {
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
        if (parent.getScriptBuffer() != null) {
            return parent.getScriptBuffer().readableBytes();
        } else {
            return 0;
        }
    }

    @Override
    public void readContent(RequestContext ctx, BinaryContentSink sink) throws Exception {
        if (parent.getScriptBuffer() != null) {
            sink.accept(parent.getScriptBuffer());
        }
        sink.close();
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        parent.deleteMember(ctx, this.id(), responder);
    }
}
