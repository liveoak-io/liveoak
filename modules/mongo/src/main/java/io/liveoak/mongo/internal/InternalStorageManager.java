package io.liveoak.mongo.internal;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import io.liveoak.mongo.config.RootMongoConfigResource;

import java.net.UnknownHostException;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class InternalStorageManager {

    DB database;

    public InternalStorageManager(RootMongoConfigResource rootMongoConfigResource) throws UnknownHostException {
        this.database = rootMongoConfigResource.getDB();
    }

    public InternalStorage getInternalStorage(String appName, String serviceName) {
        DBCollection collection = database.getCollection(appName).getCollection(serviceName);
        return new InternalStorage(collection);
    }
}

