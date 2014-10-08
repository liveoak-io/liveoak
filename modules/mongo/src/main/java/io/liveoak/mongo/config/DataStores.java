package io.liveoak.mongo.config;

import java.util.HashMap;

import java.util.Map;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class DataStores extends EmbeddedConfigResource {

    Map<String, DataStore> datastores = new HashMap<>();

    public DataStores(Resource parent) {
        super(parent);
    }

    public void addDataStore(String name, DataStore datastore) {
        datastores.put(name, datastore);
    }

    public void removeDataStore(String name) {
        datastores.remove(name);
    }

    public DataStore getDataStore(String name) {
        return datastores.get(name);
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        return datastores;
    }
}
