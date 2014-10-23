package io.liveoak.container.zero;

import java.io.File;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

/**
 * @author Ken Finnigan
 */
public class GitApplicationsResource implements RootResource, SynchronousResource {
    public GitApplicationsResource(InternalApplicationRegistry registry, File appsDir) {
        this.registry = registry;
        this.appsDir = appsDir;
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
        return "applications";
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        String gitUrl = state.getPropertyAsString("url");
        String id = state.id();

        if (gitUrl == null || gitUrl.length() == 0) {
            responder.invalidRequest(String.format(INVALID_REQUEST_MESSAGE, gitUrl));
            return;
        }

        if (id == null || id.length() == 0) {
            int start = gitUrl.lastIndexOf('/');
            int end = gitUrl.indexOf(".git", start);
            id = gitUrl.substring(start + 1, end);
        }

        File installDir = new File(this.appsDir, id);

        try {
            Git.cloneRepository()
                    .setURI(gitUrl)
                    .setDirectory(installDir)
                    .call();
        } catch (InvalidRemoteException ire) {
            responder.invalidRequest(String.format(INVALID_REQUEST_MESSAGE, gitUrl));
            return;
        } catch (TransportException te) {
            responder.invalidRequest("Unable to connect to git repo due to: " + te.getMessage());
            return;
        }

        InternalApplication app = this.registry.createApplication(id, (String) state.getProperty("name"), installDir, dir -> {
            try {
                Git gitRepo = GitHelper.initRepo(dir);
                GitHelper.addAllAndCommit(gitRepo, ctx.securityContext().getUser(), "Import LiveOak application from git: " + gitUrl);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        responder.resourceCreated(app.resource());
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        responder.readNotSupported(this);
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        responder.updateNotSupported(this);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        responder.deleteNotSupported(this);
    }

    private Resource parent;
    private final InternalApplicationRegistry registry;
    private final File appsDir;

    private static final String INVALID_REQUEST_MESSAGE = "'url' must contain a valid url to a git repository containing a single LiveOak application. '%s' is invalid.";
}
