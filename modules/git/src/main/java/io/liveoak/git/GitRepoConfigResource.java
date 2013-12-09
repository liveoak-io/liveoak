package io.liveoak.git;

import java.io.File;
import java.io.IOException;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

/**
 * @author Bob McWhirter
 */
public class GitRepoConfigResource implements Resource {

    public GitRepoConfigResource(GitRepoResource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return ";config";
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

        String repoPathStr = (String) state.getProperty("repoPath");

        if (repoPathStr == null) {
            throw new InitializationException("no git repo path specified");
        }

        File file = new File(repoPathStr);

        if (!file.canRead()) {
            throw new InitializationException("unable to readMember git repo at: " + file.getAbsolutePath());
        }

        Boolean createRepo = (Boolean) state.getProperty("createIfMissing");
        if (createRepo == null) {
            createRepo = Boolean.FALSE;
        }

        Repository repo = new RepositoryBuilder()
                .setWorkTree(file)
                .build();
        Git git = new Git(repo);

        if (!repo.getDirectory().exists()) {
            if (createRepo) {
                // No git repo, need to create
                git.getRepository().create();
            } else {
                throw new InitializationException("unable to setup repository at: " + file.getAbsolutePath()
                        + "; no git repository found");
            }
        }

        this.parent.git( git );
        this.parent.file( file );
        responder.resourceUpdated( this );
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept( "repoPath", this.parent.git().getRepository().getDirectory().getAbsolutePath() );
        sink.close();
    }

    private GitRepoResource parent;
}
