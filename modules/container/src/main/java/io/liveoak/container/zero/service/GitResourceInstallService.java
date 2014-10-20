package io.liveoak.container.zero.service;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Ken Finnigan
 */
public class GitResourceInstallService implements Service<Void> {
    @Override
    public void start(StartContext context) throws StartException {
        ResourceState gitState = new DefaultResourceState();
        gitState.putProperty("type", "git");

        context.asynchronous();
        try {
            new Thread(() -> {
                try {
                    log.debug("BOOTTIME INSTALL OF: git");
                    this.applicationInjector.getValue().extend("git", gitState, false);
                    ResourceState state = new DefaultResourceState();
                    state.putProperty("version-resource-id", "git");
                    state.putProperty("git-install-process", true);
                    //TODO When this call is made, git resource doesn't exist in ApplicationResource.members()
                    this.client.getValue().update(new RequestContext.Builder().build(), "/admin/applications/" + this.applicationInjector.getValue().id(), state);
                    context.complete();
                } catch (Throwable e) {
                    context.failed(new StartException(e));
                }
            }, "GitResourceInstallService starter - " + this.applicationInjector.getValue().name()).start();
        } catch (Throwable e) {
            context.failed(new StartException(e));
        }
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Void getValue() {
        return null;
    }

    public Injector<InternalApplication> applicationInjector() {
        return this.applicationInjector;
    }

    public Injector<Client> clientInjector() {
        return this.client;
    }

    private InjectedValue<InternalApplication> applicationInjector = new InjectedValue<>();
    private InjectedValue<Client> client = new InjectedValue<>();

    private static final Logger log = Logger.getLogger(GitResourceInstallService.class);
}
