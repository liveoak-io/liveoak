package io.liveoak.security.service;

import io.liveoak.security.integration.AuthzServiceRootResource;
import io.liveoak.security.spi.AuthzPolicyGroup;
import io.liveoak.spi.client.Client;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class AuthzResourceService implements Service<AuthzServiceRootResource> {

    public AuthzResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
       this.resource = new AuthzServiceRootResource(
               this.id,
               this.policyGroupInjector.getValue(),
               this.clientInjector.getValue()
       );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;

    }

    @Override
    public AuthzServiceRootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<Client> clientInjector() {
        return this.clientInjector;
    }

    public Injector<AuthzPolicyGroup> policyGroupInjector() {
        return this.policyGroupInjector;
    }

    private String id;

    private InjectedValue<Client> clientInjector = new InjectedValue<>();
    private InjectedValue<AuthzPolicyGroup> policyGroupInjector = new InjectedValue<>();

    private AuthzServiceRootResource resource;
}
