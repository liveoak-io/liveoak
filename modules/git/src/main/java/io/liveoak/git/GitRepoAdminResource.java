package io.liveoak.git;

import java.io.File;
import java.io.IOException;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

/**
 * @author Bob McWhirter
 */
public class GitRepoAdminResource implements RootResource {

    public GitRepoAdminResource(String id, File directory) throws IOException {
        this.id = id;
        this.directory = directory;
        this.repository = new RepositoryBuilder()
                .setWorkTree(directory)
                .build();
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
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        responder.resourceUpdated(this);
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("directory", this.directory.getAbsolutePath());
        sink.close();
    }

    File directory() {
        return this.directory;
    }

    Repository repository() {
        return this.repository;
    }

    private Resource parent;
    private final String id;
    private final File directory;
    private final Repository repository;
}
