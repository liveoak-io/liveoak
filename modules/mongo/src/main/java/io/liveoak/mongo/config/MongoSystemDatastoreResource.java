package io.liveoak.mongo.config;

import java.util.Map;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoSystemDatastoreResource extends MongoDatastoreResource {

    //Property key
    public static final String NAME = "name";

    //
    private String name;

    public MongoSystemDatastoreResource(String id) {
        super(id);
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map properties = super.properties(ctx);
        properties.put(NAME, name);

        return properties;
    }

    @Override
    public void properties(RequestContext ctx, ResourceState datastorestate) throws Exception {
        this.name = datastorestate.getProperty(NAME, true, String.class);
        super.properties(ctx, datastorestate);
    }
}
