package io.liveoak.git.service;

import io.liveoak.git.GitRepoAdminResource;
import io.liveoak.git.GitRepoResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class GitRepoResourceService implements Service<GitRepoResource> {

    public GitRepoResourceService(GitRepoAdminResource adminResource, String id) {
        this.adminResource = adminResource;
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new GitRepoResource(
                this.adminResource,
                this.id,
                this.vertxInjector.getValue()
        );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public GitRepoResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<Vertx> vertxInjector() {
        return this.vertxInjector;
    }

    private String id;
    private InjectedValue<Vertx> vertxInjector = new InjectedValue<>();
    private GitRepoResource resource;
    private final GitRepoAdminResource adminResource;

}
