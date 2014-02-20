package io.liveoak.mongo;

import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class MongoServices {

    private static ServiceName MONGO = LiveOak.LIVEOAK.append("mongo");
    private static ServiceName CLIENT = MONGO.append("client");
    private static ServiceName DB = MONGO.append("db");

    public static ServiceName mongo(String appId, String resourceId) {
        return CLIENT.append( appId, resourceId );
    }
    public static ServiceName db(String appId, String resourceId) {
        return DB.append( appId, resourceId );
    }

}
