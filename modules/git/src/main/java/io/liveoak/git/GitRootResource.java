package io.liveoak.git;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.liveoak.common.DefaultMountPointResource;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;

/**
 * @author Ken Finnigan
 */
public class GitRootResource extends DefaultMountPointResource implements GitResource {
    public GitRootResource(String id, File appDir) {
        super(id);
        this.appDir = appDir;
    }

    @Override
    public void properties(RequestContext ctx, ResourceState props) throws Exception {
        Boolean value = props.getPropertyAsBoolean("createIfAbsentOnStart");
        if (value != null) {
            this.create = value;
        }
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, Boolean> props = new HashMap<>();
        props.put("createIfAbsentOnStart", this.create);
        return props;
    }

    @Override
    public void start() throws Exception {
        try {
            this.git = Git.open(this.appDir);
        } catch (RepositoryNotFoundException e) {
            // Only create if the property has been set to enable it
            if (this.create) {
                this.git = Git.init()
                        .setDirectory(this.appDir)
                        .call();
            } else {
                // Rethrow the exception we caught if we aren't trying to create a repo
                throw e;
            }
        }

        // Install sub resources here
        registerResource(new CommitsResource(this));
    }

    @Override
    public void stop() {
        this.git.close();
        this.git = null;
        this.appDir = null;
    }

    @Override
    public Git git() {
        return this.git;
    }

    private File appDir;
    private Boolean create = Boolean.TRUE;
    private Git git;
}
