package io.liveoak.container.tenancy.service;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.Services;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class ApplicationResourcesStartupService implements Service<Void> {

    public ApplicationResourcesStartupService(ResourceState resourcesTree, Consumer<File> gitCommit, File installDir) {
        this.resourcesTree = resourcesTree;
        this.gitCommit = gitCommit;
        this.installDir = installDir;
    }

    @Override
    public void start(StartContext context) throws StartException {
        log.debug("application resources service: START: " + this.resourcesTree);
        if (this.resourcesTree == null) {
            createGitCommitService(context, null);
            return;
        }

        context.asynchronous();
        try {
            new Thread(() -> {
                try {
                    Set<ServiceName> dependencies = new HashSet<>();
                    Set<String> fields = this.resourcesTree.getPropertyNames();
                    for (String resourceId : fields) {
                        dependencies.add(Services.applicationExtension(this.applicationInjector.getValue().id(), resourceId));
                        ResourceState resourceState = (ResourceState) this.resourcesTree.getProperty(resourceId);
                        log.debug("BOOTTIME INSTALL OF: " + resourceId);
                        this.applicationInjector.getValue().extend(resourceId, resourceState, true);
                    }
                    createGitCommitService(context, dependencies);

                    context.complete();

                } catch (Throwable e) {
                    context.failed(new StartException(e));
                }
            }, "ApplicationResourcesService starter - " + this.applicationInjector.getValue().name()).start();
        } catch (Throwable e) {
            context.failed(new StartException(e));
        }
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    protected void createGitCommitService(StartContext context, Set<ServiceName> dependencies) {
        if (this.gitCommit != null) {
            ServiceTarget target = context.getChildTarget();
            ApplicationGitInstallCommitService gitService = new ApplicationGitInstallCommitService(this.gitCommit, this.installDir);
            ServiceBuilder<Void> builder = target.addService(Services.application(this.applicationInjector.getValue().id()).append("git-commit"), gitService)
                    .addDependency(Services.application(this.applicationInjector.getValue().id()));

            if (dependencies != null && dependencies.size() > 0) {
                dependencies.forEach(each -> builder.addDependency(each));
            }

            builder.install();
        }
    }

    public Injector<InternalApplication> applicationInjector() {
        return this.applicationInjector;
    }

    private ResourceState resourcesTree;
    private Consumer<File> gitCommit;
    private File installDir;
    private InjectedValue<InternalApplication> applicationInjector = new InjectedValue<>();

    private static final Logger log = Logger.getLogger(ApplicationResourcesStartupService.class);
}
