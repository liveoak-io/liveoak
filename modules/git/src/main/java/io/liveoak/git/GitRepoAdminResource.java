package io.liveoak.git;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

/**
 * @author Bob McWhirter
 */
public class GitRepoAdminResource implements RootResource, SynchronousResource {

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
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, String> result = new HashMap<>();
        result.put("directory", this.directory.getAbsolutePath());
        return result;
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
