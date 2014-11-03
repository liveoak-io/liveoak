package io.liveoak.container.extension;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.resource.async.DelegatingResponder;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.security.SecurityContext;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Ken Finnigan
 */
public class ConfigVersioningResponder extends DelegatingResponder {
    public ConfigVersioningResponder(Responder delegate, boolean versioned, String path, Client client, SecurityContext securityContext) {
        super(delegate);
        this.path = path;
        this.versioned = versioned;
        this.client = client;
        this.securityContext = securityContext;
    }

    @Override
    public void resourceCreated(Resource resource) {
        if (versioned) {
            // Versioning is installed
            performCommit(resource, RequestType.CREATE, () -> super.resourceCreated(resource));
        } else {
            super.resourceCreated(resource);
        }
    }

    @Override
    public void resourceUpdated(Resource resource) {
        if (versioned) {
            // Versioning is installed
            performCommit(resource, RequestType.UPDATE, () -> super.resourceUpdated(resource));
        } else {
            super.resourceUpdated(resource);
        }
    }

    @Override
    public void resourceDeleted(Resource resource) {
        if (versioned) {
            // Versioning is installed
            performCommit(resource, RequestType.DELETE, () -> super.resourceDeleted(resource));
        } else {
            super.resourceDeleted(resource);
        }
    }

    private void performCommit(Resource resource, RequestType type, Runnable success) {
        ResourceState state = new DefaultResourceState();
        state.putProperty("msg", commitMsg(resource, type));

        this.client.create(new RequestContext.Builder().securityContext(this.securityContext).build(), path(), state, (response) -> {
            if (response.responseType() == ClientResourceResponse.ResponseType.OK) {
                success.run();
            } else {
                super.error(ResourceErrorResponse.ErrorType.INTERNAL_ERROR, "Unable to commit changes due to: " + response.responseType());;
            }
        });
    }

    private String path() {
        return this.path;
    }

    private String commitMsg(Resource resource, RequestType type) {
        return "Configuration modified for " + type + " request on resource with id " + resource.id();
    }

    private boolean versioned;
    private String path;
    private Client client;
    private SecurityContext securityContext;
}
