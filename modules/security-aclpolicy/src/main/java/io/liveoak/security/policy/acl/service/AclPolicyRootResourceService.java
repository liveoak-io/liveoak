package io.liveoak.security.policy.acl.service;

import io.liveoak.security.policy.acl.impl.AclPolicy;
import io.liveoak.security.policy.acl.integration.AclPolicyRootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class AclPolicyRootResourceService implements Service<AclPolicyRootResource> {

    public AclPolicyRootResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new AclPolicyRootResource(
                this.id,
                this.policyInjector.getValue()
        );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public AclPolicyRootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<AclPolicy> policyInjector() {
        return this.policyInjector;
    }

    private String id;

    private InjectedValue<AclPolicy> policyInjector = new InjectedValue<>();

    private AclPolicyRootResource resource;
}
