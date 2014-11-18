package io.liveoak.ups.resource.config;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class UPSRootConfigResource implements RootResource {

    Resource parent;
    String id;

    public static final String UPS_SERVER_URL = "upsURL";
    public static final String APPLICATION_ID = "applicationId";
    public static final String MASTER_SECRET = "masterSecret";

    private String upsServerURL;
    private String applicationId;
    private String masterSecret;

    public UPSRootConfigResource(String id) {
        this.id = id;
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
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(UPS_SERVER_URL, upsServerURL);
        sink.accept(APPLICATION_ID, applicationId);
        sink.accept(MASTER_SECRET, masterSecret);
        sink.complete();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        Object upsServerURLProperty = state.getProperty(UPS_SERVER_URL);
        Object applicationIdProperty = state.getProperty(APPLICATION_ID);
        Object masterKeyProperty = state.getProperty(MASTER_SECRET);

        if (upsServerURLProperty instanceof String) {
            this.upsServerURL = (String) upsServerURLProperty;
        } else if (upsServerURLProperty != null) { // if it is null, it wasn't specified in the json, so keep old value
            responder.invalidRequest("The " + UPS_SERVER_URL + " property must be a String.");
        }

        if (applicationIdProperty instanceof String) {
            this.applicationId = (String) applicationIdProperty;
        } else if (applicationIdProperty != null) { // if it is null, it wasn't specified in the json, so keep old value
            responder.invalidRequest("The " + APPLICATION_ID + " property must be a String.");
        }

        if (masterKeyProperty instanceof String) {
            this.masterSecret = (String) masterKeyProperty;
        } else if (masterKeyProperty != null) { // if it is null, it wasn't specified in the json, so keep old value
            responder.invalidRequest("The " + MASTER_SECRET + " property must be a String.");
        }
        responder.resourceUpdated(this);
    }


    public String getUPSServerURL() {
        return upsServerURL;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getMasterSecret() {
        return masterSecret;
    }
}
