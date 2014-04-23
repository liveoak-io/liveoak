package io.liveoak.security.policy.acl.integration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.liveoak.common.security.AuthzConstants;
import io.liveoak.security.policy.acl.AclPolicyConstants;
import io.liveoak.security.policy.acl.impl.AclPolicy;
import io.liveoak.security.policy.acl.impl.AclPolicyConfig;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyRootResource implements RootResource, SynchronousResource {

    private Resource parent;
    private String id;

    private final Map<String, Resource> children = new HashMap<>();

    public AclPolicyRootResource(String id, AclPolicy policy) {
        this.id = id;
        this.children.put(AuthzConstants.AUTHZ_CHECK_RESOURCE_ID, new AclPolicyCheckResource(this, AuthzConstants.AUTHZ_CHECK_RESOURCE_ID, policy));
        this.children.put(AclPolicyConstants.RESOURCE_CREATION_LISTENER_RESOURCE_ID, new AclCreationListenerResource(this, AclPolicyConstants.RESOURCE_CREATION_LISTENER_RESOURCE_ID, policy));
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Resource member(String id) {
        return children.get(id);
    }

    @Override
    public Collection<? extends Resource> members() {
        return children.values();
    }
}
