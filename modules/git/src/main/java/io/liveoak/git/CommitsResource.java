package io.liveoak.git;

import java.util.Collection;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.security.UserProfile;
import io.liveoak.spi.state.ResourceState;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author Ken Finnigan
 */
public class CommitsResource implements SynchronousResource {

    public CommitsResource(GitResource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return "commits";
    }

    @Override
    public Collection<? extends Resource> members(RequestContext ctx) throws Exception {
        //TODO
        return null;
    }

    @Override
    public Resource member(RequestContext ctx, String id) throws Exception {
        //TODO
        return null;
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        String commitMsg = state.getPropertyAsString("msg");
        Boolean includeUntracked = state.getPropertyAsBoolean("include-untracked");

        if (includeUntracked == null) {
            // Default to include all untracked files
            includeUntracked = Boolean.TRUE;
        }

        // Add changed files to staging ready for commit
        AddCommand addCmd = parent.git().add().addFilepattern(".");

        if (!includeUntracked) {
            // This will prevent new files from being added to the index, and therefore the commit
            addCmd.setUpdate(true);
        }

        addCmd.call();

        // Commit staged changes
        CommitCommand commitCmd = parent.git().commit();

        UserProfile user = ctx.securityContext().getUser();
        if (user != null && user.name() != null && user.email() != null) {
            commitCmd.setCommitter(user.name(), user.email());
        }

        if (commitMsg != null) {
            commitCmd.setMessage(commitMsg);
        }

        RevCommit commit = commitCmd.call();

        responder.resourceCreated(new GitCommitResource(this, commit));
    }

    GitResource parent;
}
