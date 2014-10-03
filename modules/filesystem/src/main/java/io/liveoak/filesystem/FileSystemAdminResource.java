package io.liveoak.filesystem;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public class FileSystemAdminResource implements RootResource, SynchronousResource {

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
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, String> result = new HashMap<>();
        result.put("directory", this.directory.getAbsolutePath());
        return result;
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
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
