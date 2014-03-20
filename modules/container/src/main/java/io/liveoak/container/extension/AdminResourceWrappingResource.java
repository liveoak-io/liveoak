package io.liveoak.container.extension;

import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.net.URI;

/**
 * @author Bob McWhirter
 */
public class AdminResourceWrappingResource implements RootResource {

    public AdminResourceWrappingResource(InternalApplicationExtension extension, ApplicationConfigurationManager configManager, RootResource delegate, boolean boottime) {
        this.extension = extension;
        this.configManager = configManager;
        this.delegate = delegate;
        this.ignoreUpdate = boottime;
    }

    public String type() {
        return this.extension.extensionId();
    }

    public ApplicationConfigurationManager configurationManager() {
        return this.configManager;
    }

    @Override
    public Resource parent() {
        return this.delegate.parent();
    }

    @Override
    public String id() {
        return this.delegate.id();
    }

    @Override
    public void parent(Resource parent) {
        this.delegate.parent(parent);
    }

    @Override
    public URI uri() {
        return this.delegate.uri();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        if (this.ignoreUpdate) {
            this.ignoreUpdate = false;
            this.delegate.updateProperties(ctx, state, responder);
        } else {
            this.delegate.updateProperties(ctx, state, new ConfigPersistingResponder(this, state, responder));
        }
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        this.delegate.readProperties(ctx, sink);
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        this.delegate.createMember(ctx, state, responder);
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        this.delegate.readMembers(ctx, sink);
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        this.delegate.readMember(ctx, id, responder);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.extension.remove();
        // TODO should we call delete() on this resource?  I think yes.
        //this.delegate.delete(ctx, responder);
        responder.resourceDeleted(this.delegate);
        this.configManager.removeResource(this.delegate.id());
    }

    private final InternalApplicationExtension extension;
    private final RootResource delegate;
    private final ApplicationConfigurationManager configManager;
    private boolean ignoreUpdate;


}
