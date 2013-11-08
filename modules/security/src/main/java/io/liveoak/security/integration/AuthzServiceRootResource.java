package io.liveoak.security.integration;

import io.liveoak.container.auth.AuthzConstants;
import io.liveoak.container.auth.SimpleLogger;
import io.liveoak.security.spi.AuthzPolicyEntry;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;

import java.util.List;

/**
 * Root resource to be registered in DefaultContainer
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzServiceRootResource implements RootResource {

    private static final SimpleLogger log = new SimpleLogger(AuthzServiceRootResource.class);

    private String id;

    private AuthzCheckResource authzCheckResource;
    private AuthzServiceConfigResource configResource = new AuthzServiceConfigResource(this);

    private Client client;

    public AuthzServiceRootResource(String id) {
        this.id = id;
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        client = context.client();
        setPolicies(configResource.getPolicies());
    }

    @Override
    public void destroy() {
        // Nothing here for now
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        try {
            if (id.equals(AuthzConstants.AUTHZ_CHECK_RESOURCE_ID)) {
                responder.resourceRead(authzCheckResource);
            } else {
                responder.noSuchResource(id);
            }
        } catch (Throwable t) {
            responder.internalError(t.getMessage());
        }
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        sink.accept(authzCheckResource);
        sink.close();
    }

    public Client getClient() {
        return client;
    }

    @Override
    public Resource configuration() {
        return configResource;
    }

    public void setPolicies(List<AuthzPolicyEntry> policies) {
        authzCheckResource = new AuthzCheckResource(AuthzConstants.AUTHZ_CHECK_RESOURCE_ID, policies, this);
    }
}
