package io.liveoak.mongo.internal;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import io.liveoak.mongo.config.RootMongoConfigResource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class InternalStorageManager {

    RootMongoConfigResource mongoSystemConfigResource;

    public InternalStorageManager(RootMongoConfigResource mongoSystemConfigResource) throws UnknownHostException {
        this.mongoSystemConfigResource = mongoSystemConfigResource;
    }

    public InternalStorage getInternalStorage(String appName, String serviceName) {
        appName = replaceCollectionName(appName);
        serviceName = replaceCollectionName(serviceName);
        DB database = mongoSystemConfigResource.getDB();
        DBCollection collection = database.getCollection(appName).getCollection(serviceName);
        return new InternalStorage(collection);
    }

    // Collection names like "acl-policy" are causing problems
    public static String replaceCollectionName(String collectionName) {
        return collectionName.replaceAll("-", "_");
    }
}

