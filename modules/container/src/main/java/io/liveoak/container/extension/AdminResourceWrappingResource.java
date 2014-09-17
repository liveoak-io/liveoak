package io.liveoak.container.extension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.util.ConversionUtils;
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

    public AdminResourceWrappingResource(InternalApplicationExtension extension, ApplicationConfigurationManager configManager, RootResource delegate, boolean ignoreUpdate) {
        super(delegate);
        this.extension = extension;
        this.configManager = configManager;
        this.ignoreUpdate = ignoreUpdate;
    }

    public String type() {
        return this.extension.extensionId();
    }

    public ApplicationConfigurationManager configurationManager() {
        return this.configManager;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(LiveOak.RESOURCE_TYPE, type());
        JsonNode configNode = this.configManager.readResource(super.id()).get("config");
        ResourceState configState = configNode != null ? ConversionUtils.convert((ObjectNode)configNode) : new DefaultResourceState();

        boolean runtimeValuePresent = ctx.resourceParams().value("runtime") != null;
        boolean runtimeRequested = runtimeValuePresent ? Boolean.parseBoolean(ctx.resourceParams().value("runtime")) : ctx.resourceParams().names().contains("runtime");

        // If runtime is set, we return the filtered values. If it isn't, we return the unfiltered
        if (runtimeRequested) {
            super.readProperties(ctx, sink);
        } else {
            super.readProperties(ctx, new PropertySink() {
                @Override
                public void accept(String name, Object value) {
                    Object nonParsedValue;
                    if ((nonParsedValue = configState.getProperty(name)) != null) {
                        sink.accept(name, nonParsedValue);
                    } else {
                        sink.accept(name, value);
                    }
                }

                @Override
                public void close() throws Exception {
                    sink.close();
                }
            });
        }
    }

    @Override
    public void initializeProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        cleanupState(state);
        delegate().initializeProperties(ctx, state, responder);
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        cleanupState(state);

        if (this.ignoreUpdate) {
            this.ignoreUpdate = false;
            delegate().initializeProperties(ctx, state, responder);
        } else {
            delegate().updateProperties(ctx, state, new ResourceConfigPersistingResponder(this, state, responder));
        }
    }

    private void cleanupState(ResourceState state) {
        //Clean out from the state what we don't care about
        state.removeProperty(LiveOak.ID);
        state.removeProperty(LiveOak.SELF);
        state.removeProperty(LiveOak.RESOURCE_TYPE);
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
    private boolean ignoreUpdate;


}
