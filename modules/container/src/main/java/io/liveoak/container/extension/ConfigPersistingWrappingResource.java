package io.liveoak.container.extension;

import java.util.Properties;

import io.liveoak.common.util.JsonFilterUtils;
import io.liveoak.common.util.ObjectsTree;
import io.liveoak.container.tenancy.ConfigurationManager;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.DelegatingRootResource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.DelegatingResponder;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public abstract class ConfigPersistingWrappingResource extends DelegatingRootResource implements PropertyReplacementMapper {

    public ConfigPersistingWrappingResource(ConfigurationManager configManager, RootResource delegate, Properties envProps, Client client) {
        super(delegate);
        this.configManager = configManager;
        this.environmentProperties = envProps;
        this.client = client;
    }

    public abstract String extensionId();

    public abstract boolean resourceVersioned();

    public abstract String versionedResourcePath();

    public ConfigurationManager configurationManager() {
        return this.configManager;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        readConfigEnvVars(this.configValuesTree, ctx, sink);
        sink.accept(LiveOak.RESOURCE_TYPE, extensionId());
        super.readProperties(ctx, sink);
    }

    @Override
    public void initializeProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        cleanup(state);
        this.configValuesTree = storeConfigEnvVars(state);
        delegate().initializeProperties(ctx, filter(state), responder);
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        cleanup(state);
        this.configValuesTree = storeConfigEnvVars(state);
        delegate().updateProperties(ctx, filter(state),
                new RootResourceConfigPersistingResponder(this, state,
                        new ConfigVersioningResponder(responder, resourceVersioned(), versionedResourcePath(), this.client, ctx.securityContext())
                )
        );
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        delegate().createMember(ctx, filter(state), new ConfigVersioningResponder(responder, resourceVersioned(), versionedResourcePath(), this.client, ctx.securityContext()));
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        configManager.removeResource(id());
        new ConfigVersioningResponder(responder, resourceVersioned(), versionedResourcePath(), this.client, ctx.securityContext()).resourceDeleted(this.delegate());
    }

    protected class DeleteResponder extends DelegatingResponder {

        public DeleteResponder(Responder delegate) {
            super(delegate);
        }

        @Override
        public void resourceDeleted(Resource resource) {
            try {
                configManager.removeResource(id());
            } catch (Exception e) {
                //TODO Fix
                e.printStackTrace();
            }
            super.resourceDeleted(resource);
        }
    }

    //TODO Do this in container maybe???
    protected void cleanup(ResourceState state) {
        //Clean out from the state what we don't care about
        state.removeProperty(LiveOak.ID);
        state.removeProperty(LiveOak.SELF);
        state.removeProperty(LiveOak.RESOURCE_TYPE);
    }

    protected ResourceState filter(ResourceState state) {
        return JsonFilterUtils.filter(state, this.environmentProperties);
    }

    protected final ConfigurationManager configManager;
    protected Client client;
    protected ObjectsTree<Object> configValuesTree;
    private Properties environmentProperties;

}
