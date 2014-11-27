package io.liveoak.mongo.config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDatastoresRegistry {

    Map<String, MongoSystemDatastoreResource> datastores = new HashMap<>();

    public MongoSystemDatastoreResource getDataStore(String name) {
        return datastores.get(name);
    }

    public void addDataStore(String id, MongoSystemDatastoreResource dataStore) {
        datastores.put(id, dataStore);
    }
}
