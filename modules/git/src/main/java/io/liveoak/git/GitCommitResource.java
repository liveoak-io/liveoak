package io.liveoak.git;

import io.liveoak.spi.exceptions.UpdateNotSupportedException;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author Ken Finnigan
 */
public class GitCommitResource implements SynchronousResource {

    public GitCommitResource(CommitsResource parent, RevCommit commit) {
        this.commit = commit;
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.commit.getName();
    }

    @Override
    public void properties(ResourceState props) throws Exception {
        throw new UpdateNotSupportedException(this.uri().getPath());
    }

    private RevCommit commit;
    private CommitsResource parent;
}
