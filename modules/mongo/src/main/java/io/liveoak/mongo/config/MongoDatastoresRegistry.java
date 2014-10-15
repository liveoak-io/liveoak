package io.liveoak.mongo.config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDatastoresRegistry {

    Map<String, MongoDatastoreResource> datastores = new HashMap<>();

    public MongoDatastoreResource getDataStore(String name) {
        return datastores.get(name);
    }

    public void addDataStore(String id, MongoDatastoreResource dataStore) {
        datastores.put(id, dataStore);
    }
}
