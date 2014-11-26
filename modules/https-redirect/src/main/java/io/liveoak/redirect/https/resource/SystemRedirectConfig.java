package io.liveoak.redirect.https.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.PropertyException;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class SystemRedirectConfig implements RootResource {

    Resource parent;
    String id;

    private static final String DEFAULT = "default";

    private static final Integer DEFAULT_MAX_AGE = 300; // 5 minutes

    private Redirect defaultRedirect;

    public SystemRedirectConfig(String id) {
        this.id = id;
        this.defaultRedirect = new Redirect(Redirect.Options.SECURED, Redirect.Types.TEMPORARY, DEFAULT_MAX_AGE);
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void initializeProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        setProperties(ctx, state, responder, true);
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        setProperties(ctx, state, responder, false);
    }

    protected void setProperties(RequestContext ctx, ResourceState state, Responder responder, boolean initial) {
        try {
            ResourceState defaultResourceState = state.getProperty(DEFAULT, true, ResourceState.class);
            Redirect.Options option = defaultResourceState.getProperty(Redirect.REDIRECT, true, Redirect.Options.class);
            Redirect.Types defaultType = defaultResourceState.getProperty(Redirect.TYPE, true, Redirect.Types.class);

            Integer maxAge = defaultResourceState.getProperty(Redirect.MAX_AGE, true, Integer.class);

            if (maxAge < 0) {
                if (initial) {
                    log.error("The max-age integer value mustbe positive.");
                } else {
                    responder.invalidRequest("The max-age integer value must be positive.");
                    return;
                }
            }

            Redirect defaultRedirect = new Redirect(option, defaultType, maxAge);

            this.defaultRedirect = defaultRedirect;

            responder.resourceUpdated(this);

        } catch (PropertyException e) {
            if (initial) {
                log.error("Invalid property values were specified. Falling back to defaults.", e);
                responder.resourceUpdated(this);
            } else {
                responder.invalidRequest("The specified properties are invalid", e);
            }
        }
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(DEFAULT, defaultRedirect);
        sink.complete();
    }

    public Redirect getDefaultRedirect() {
        return this.defaultRedirect;
    }
}
