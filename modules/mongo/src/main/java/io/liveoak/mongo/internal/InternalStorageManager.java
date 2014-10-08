package io.liveoak.mongo.internal;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import io.liveoak.mongo.config.MongoSystemConfigResource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class InternalStorageManager {

    MongoSystemConfigResource mongoSystemConfigResource;

    public InternalStorageManager(MongoSystemConfigResource mongoSystemConfigResource) throws UnknownHostException {
        this.mongoSystemConfigResource = mongoSystemConfigResource;
    }

    public InternalStorage getInternalStorage(String appName, String serviceName) {
        appName = replaceCollectionName(appName);
        serviceName = replaceCollectionName(serviceName);
        DB database = mongoSystemConfigResource.getSystemDatabase();
        DBCollection collection = database.getCollection(appName).getCollection(serviceName);
        return new InternalStorage(collection);
    }

    // Collection names like "acl-policy" are causing problems
    public static String replaceCollectionName(String collectionName) {
        return collectionName.replaceAll("-", "_");
    }
}

