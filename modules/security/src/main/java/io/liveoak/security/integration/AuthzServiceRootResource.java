package io.liveoak.security.integration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import io.liveoak.common.security.AuthzConstants;
import io.liveoak.security.spi.AuthzServiceConfig;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.logging.Logger;

/**
 * Root resource to be registered in DefaultContainer
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzServiceRootResource implements RootResource, SynchronousResource {

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
    public Resource member(RequestContext ctx, String id) {
        if (id.equals(this.authzCheckResource.id())) {
            return authzCheckResource;
        }
        return null;
    }

    @Override
    public Collection<? extends Resource> members(RequestContext ctx) {
        LinkedList<Resource> members = new LinkedList<>();
        members.add(authzCheckResource);
        return members;
    }
}
