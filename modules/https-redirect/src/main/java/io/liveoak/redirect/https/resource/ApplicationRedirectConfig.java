package io.liveoak.redirect.https.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.PropertyException;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ApplicationRedirectConfig extends Redirect implements RootResource {

    public ApplicationRedirectConfig(String id) {
        super(id);
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
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

            Options redirect= state.getProperty(REDIRECT, false, Options.class);
            Types type = state.getProperty(TYPE, false, Types.class);

            Integer maxAge = state.getProperty(MAX_AGE, false, Integer.class);

            if (maxAge != null && maxAge < 0) {
                responder.invalidRequest("The max-age integer must be positive.");
                return;
            }

            this.option = redirect;
            this.type = type;
            this.maxAge = maxAge;
            responder.resourceUpdated(this);

        } catch (PropertyException pe) {
            responder.invalidRequest("The specified properties are invalid.",pe);
        }
    }
}
