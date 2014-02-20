package io.liveoak.filesystem;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class FileSystemAdminResource implements RootResource {

    public FileSystemAdminResource(String id, File directory) {
        this.id = id;
        this.directory = directory;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("directory", this.directory.getAbsolutePath());
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        System.err.println("Aggregating FS: " + this.id + " // " + state);
        String directory = (String) state.getProperty("directory");
        if (this.directory == null && (directory == null || directory.trim().equals(""))) {
            responder.invalidRequest("'directory' may not be empty");
            return;
        }
        if (directory != null) {
            this.directory = new File(directory.trim());
        }
        responder.resourceUpdated(this);
    }

    public File directory() {
        return this.directory;
    }

    private final String id;
    private Resource parent;
    private File directory;
}
