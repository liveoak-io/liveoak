package io.liveoak.container.zero;

import java.io.File;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.common.DefaultMountPointResource;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.eclipse.jgit.api.Git;

/**
 * @author Bob McWhirter
 */
public class ApplicationsResource extends DefaultMountPointResource {

    public ApplicationsResource(InternalApplicationRegistry applicationRegistry) {
        super("applications");
        this.applicationRegistry = applicationRegistry;
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        File dir = null;
        Git gitRepo = null;
        String dirPath = (String) state.getProperty("dir");
        if (dirPath != null) {
            dir = new File(dirPath);
            gitRepo = GitHelper.initRepo(dir);
        }

        InternalApplication app = this.applicationRegistry.createApplication(state.id(), (String) state.getProperty("name"), dir);

        if (gitRepo == null) {
            gitRepo = GitHelper.initRepo(app.directory());
        }

        GitHelper.addAllAndCommit(gitRepo, ctx.securityContext().getUser(), "Initial creation of LiveOak application");

        responder.resourceCreated(app.resource());
    }

    private final InternalApplicationRegistry applicationRegistry;


}
