package io.liveoak.filesystem;

import java.io.File;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.config.ConfigResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class FilesystemConfigResource implements ConfigResource {

    public FilesystemConfigResource(FilesystemResource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept( "file", this.parent.file().getAbsolutePath() );
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        String rootStr = state.getProperty("root").toString();

        if (rootStr == null) {
            throw new InitializationException("no filesystem root specified");
        }

        File file = new File(rootStr);

        if (!file.canRead()) {
            throw new InitializationException("unable to readMember filesystem at: " + file.getAbsolutePath());
        }

        this.parent.file( file );
        responder.resourceUpdated( this );
    }

    private FilesystemResource parent;
}
