package io.liveoak.security.policy.acl.service;

import io.liveoak.security.policy.acl.AclPolicyConfig;
import io.liveoak.security.policy.acl.AclPolicyRootResource;
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
public class AclPolicyRootResourceService implements Service<AclPolicyRootResource> {

    public AclPolicyRootResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new AclPolicyRootResource(
                this.id ,
                this.policyInjector.getValue(),
                this.clientInjector.getValue()
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

    public Injector<AclPolicyConfig> policyInjector() {
        return this.policyInjector;
    }

    public Injector<Client> clientInjector() {
        return this.clientInjector;
    }

    private String id;

    private InjectedValue<AclPolicyConfig> policyInjector = new InjectedValue<>();
    private InjectedValue<Client> clientInjector = new InjectedValue<>();

    private AclPolicyRootResource resource;
}
