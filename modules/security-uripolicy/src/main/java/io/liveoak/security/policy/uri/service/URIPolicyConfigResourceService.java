package io.liveoak.security.policy.uri.service;

import io.liveoak.security.policy.uri.impl.URIPolicy;
import io.liveoak.security.policy.uri.integration.URIPolicyConfigResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyConfigResourceService implements Service<URIPolicyConfigResource> {

    public URIPolicyConfigResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new URIPolicyConfigResource( this.id, this.policyInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public URIPolicyConfigResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<URIPolicy> policyInjector() {
        return this.policyInjector;
    }

    private String id;
    private InjectedValue<URIPolicy> policyInjector = new InjectedValue<>();
    private URIPolicyConfigResource resource;
}
