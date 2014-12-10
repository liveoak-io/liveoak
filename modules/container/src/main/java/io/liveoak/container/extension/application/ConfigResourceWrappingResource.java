package io.liveoak.container.extension.application;

import java.io.File;

import io.liveoak.common.util.ObjectsTree;
import io.liveoak.container.extension.ConfigVersioningResponder;
import io.liveoak.container.extension.PropertyReplacementMapper;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.async.DelegatingResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigRootResource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Ken Finnigan
 */
public class ConfigResourceWrappingResource extends DelegatingResource implements PropertyReplacementMapper {

    public ConfigResourceWrappingResource(Resource delegate, InternalApplication application, Client client) {
        super(delegate);
        this.application = application;
        this.client = client;
        this.extensionConfigDirectory = findRoot(delegate).baseConfig(application.configurationDirectory());
    }

    private ConfigRootResource findRoot(Resource delegate) {
        Resource temp = delegate;
        while (!(temp instanceof ConfigRootResource)) {
            temp = delegate.parent();
        }
        return (ConfigRootResource) temp;
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        delegate().createMember(ctx, state,
                new ConfigResourcePersistingResponder(state, extensionConfigDirectory,
                        new ConfigVersioningResponder(responder, application.versioned(), application.versionedResourcePath(), client, ctx.securityContext())
                )
        );
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        // Delete all child resource configurations, and it's own
        delegate().delete(ctx,
                new ConfigResourcePersistingResponder(null, extensionConfigDirectory,
                        new ConfigVersioningResponder(responder, application.versioned(), application.versionedResourcePath(), client, ctx.securityContext())
                )
        );
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        delegate().readMember(ctx, id, new WrapConfigResourceResponder(responder, application, client));
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        readConfigEnvVars(configValuesTree, ctx, sink);
        delegate().readProperties(ctx, sink);
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        this.configValuesTree = storeConfigEnvVars(state);
        //TODO Filter state
        delegate().updateProperties(ctx, state,
            new ConfigResourcePersistingResponder(state, extensionConfigDirectory,
                    new ConfigVersioningResponder(responder, application.versioned(), application.versionedResourcePath(), client, ctx.securityContext())
            )
        );
    }

    private InternalApplication application;
    private Client client;
    private ObjectsTree<Object> configValuesTree;
    private File extensionConfigDirectory;
}
