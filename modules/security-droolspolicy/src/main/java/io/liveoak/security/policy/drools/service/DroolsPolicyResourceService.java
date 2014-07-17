package io.liveoak.security.policy.drools.service;

import io.liveoak.security.policy.drools.impl.DroolsPolicy;
import io.liveoak.security.policy.drools.integration.DroolsPolicyRootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class DroolsPolicyResourceService implements Service<DroolsPolicyRootResource> {

    public DroolsPolicyResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new DroolsPolicyRootResource(this.id, this.policyInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public DroolsPolicyRootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<DroolsPolicy> policyInjector() {
        return this.policyInjector;
    }

    private String id;
    private InjectedValue<DroolsPolicy> policyInjector = new InjectedValue<>();
    private DroolsPolicyRootResource resource;

}
