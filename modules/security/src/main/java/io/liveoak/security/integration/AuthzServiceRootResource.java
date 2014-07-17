package io.liveoak.security.integration;

import java.util.concurrent.atomic.AtomicReference;

import io.liveoak.common.security.AuthzConstants;
import io.liveoak.security.spi.AuthzServiceConfig;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import org.jboss.logging.Logger;

/**
 * Root resource to be registered in DefaultContainer
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzServiceRootResource implements RootResource {

    private static final Logger log = Logger.getLogger(AuthzServiceRootResource.class);

    private Resource parent;

    private String id;

    private AuthzCheckResource authzCheckResource;

    private final AtomicReference<AuthzServiceConfig> config = new AtomicReference<>();

    public AuthzServiceRootResource(String id, Client client) {
        this.id = id;
        this.authzCheckResource = new AuthzCheckResource(this, AuthzConstants.AUTHZ_CHECK_RESOURCE_ID, client);
    }

    public void setConfig(AuthzServiceConfig config) {
        this.config.set(config);
        log.debug("Security configuration updated: " + config);
    }

    public AuthzServiceConfig getConfig() {
        return config.get();
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
        if (id.equals(this.authzCheckResource.id())) {
            responder.resourceRead(authzCheckResource);
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        sink.accept(authzCheckResource);
        sink.close();
    }
}
