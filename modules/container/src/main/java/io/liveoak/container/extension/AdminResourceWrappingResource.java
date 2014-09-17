package io.liveoak.container.extension;

import java.util.Properties;

import io.liveoak.common.util.JsonFilterUtils;
import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.DelegatingRootResource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class AdminResourceWrappingResource extends DelegatingRootResource {

    public AdminResourceWrappingResource(InternalApplicationExtension extension, ApplicationConfigurationManager configManager, RootResource delegate, Properties envProps) {
        super(delegate);
        this.extension = extension;
        this.configManager = configManager;
        this.environmentProperties = envProps;
    }

    public String type() {
        return this.extension.extensionId();
    }

    public ApplicationConfigurationManager configurationManager() {
        return this.configManager;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        boolean runtimeValuePresent = ctx.resourceParams().value("runtime") != null;
        boolean runtimeRequested = runtimeValuePresent ? Boolean.parseBoolean(ctx.resourceParams().value("runtime")) : ctx.resourceParams().names().contains("runtime");

        // If runtime is set, we set the environment variables for replacement
        if (!runtimeRequested) {
            sink.replaceWith(this.environmentProperties);
        }

        sink.accept(LiveOak.RESOURCE_TYPE, type());
        super.readProperties(ctx, sink);
    }

    @Override
    public void initializeProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        cleanup(state);
        delegate().initializeProperties(ctx, filter(state), responder);
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        cleanup(state);

        delegate().updateProperties(ctx, filter(state), new ResourceConfigPersistingResponder(this, state, responder));
    }

    private void cleanup(ResourceState state) {
        //Clean out from the state what we don't care about
        state.removeProperty(LiveOak.ID);
        state.removeProperty(LiveOak.SELF);
        state.removeProperty(LiveOak.RESOURCE_TYPE);
    }

    private ResourceState filter(ResourceState state) {
        return JsonFilterUtils.filter(state, this.environmentProperties);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.extension.remove();
        // TODO should we call delete() on this resource?  I think yes.
        //this.delegate.delete(ctx, responder);
        this.configManager.removeResource(this.id());
        responder.resourceDeleted(this.delegate());
    }

    private final InternalApplicationExtension extension;
    private final ApplicationConfigurationManager configManager;
    private Properties environmentProperties;

}
