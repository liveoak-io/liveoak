package io.liveoak.application.clients.service;

import io.liveoak.application.clients.ApplicationClientsResource;
import io.liveoak.keycloak.client.DirectAccessClient;
import io.liveoak.keycloak.client.SecurityClient;
import io.liveoak.spi.Application;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Ken Finnigan
 */
public class ApplicationClientsResourceService implements Service<RootResource> {
    public ApplicationClientsResourceService(Application application) {
        this.application = application;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new ApplicationClientsResource(this.application, this.securityClientInjector.getValue(), this.directAccessClientInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
        this.application = null;
    }

    @Override
    public RootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<SecurityClient> securityClientInjector() {
        return this.securityClientInjector;
    }

    public Injector<DirectAccessClient> directAccessClientInjector() {
        return this.directAccessClientInjector;
    }

    private RootResource resource;
    private Application application;
    private InjectedValue<SecurityClient> securityClientInjector = new InjectedValue<>();
    private InjectedValue<DirectAccessClient> directAccessClientInjector = new InjectedValue<>();
}
