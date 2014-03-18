package io.liveoak.security.policy.acl;

import io.liveoak.common.security.AuthzConstants;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyRootResource implements RootResource {

    private static final Logger log = Logger.getLogger(AclPolicyRootResource.class);

    private Resource parent;
    private String id;

    private AclPolicyCheckResource checkResource;

    private String configFile;
    private AclPolicyConfig policyConfig;

    public AclPolicyRootResource(String id, AclPolicyConfig policy, Client client) {
        this.id = id;
        this.checkResource = new AclPolicyCheckResource(this, AuthzConstants.AUTHZ_CHECK_RESOURCE_ID, policy, client);
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
    public void readMember(RequestContext ctx, String id, Responder responder) {
        if (id.equals(this.checkResource.id())) {
            responder.resourceRead(this.checkResource);
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        sink.accept(this.checkResource);
        sink.close();
    }
}
