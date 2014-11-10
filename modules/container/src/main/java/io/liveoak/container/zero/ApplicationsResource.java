package io.liveoak.container.zero;

import java.io.File;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.common.DefaultMountPointResource;
import io.liveoak.container.zero.git.GitHelper;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.eclipse.jgit.api.Git;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class ApplicationsResource extends DefaultMountPointResource {

    public ApplicationsResource(InternalApplicationRegistry applicationRegistry) {
        super("applications");
        this.applicationRegistry = applicationRegistry;
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        File dir = null;
        String dirPath = (String) state.getProperty("dir");
        if (dirPath != null) {
            dir = new File(dirPath);
            GitHelper.initRepo(dir);
        }

        InternalApplication app = this.applicationRegistry.createApplication(state.id(), (String) state.getProperty("name"), dir, installDir -> {
            try {
                log.debug("Init git repo for: " + installDir);
                Git gitRepo = GitHelper.initRepo(installDir);
                GitHelper.addAllAndCommit(gitRepo, ctx.securityContext().getUser(), "Initial creation of LiveOak application");
                gitRepo.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        responder.resourceCreated(app.resource());
    }

    private final InternalApplicationRegistry applicationRegistry;

    private static final Logger log = Logger.getLogger(ApplicationsResource.class);
}
