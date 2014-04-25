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
        appName = replaceCollectionName(appName);
        serviceName = replaceCollectionName(serviceName);
        DBCollection collection = database.getCollection(appName).getCollection(serviceName);
        return new InternalStorage(collection);
    }

    // Collection names like "acl-policy" are causing problems
    public static String replaceCollectionName(String collectionName) {
        return collectionName.replaceAll("-", "_");
    }
}

