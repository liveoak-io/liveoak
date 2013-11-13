package io.liveoak.mongo;

import com.mongodb.*;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
//public class MongoResource implements CollectionResource, RootResource {
//public class MongoResource implements RootResource {
public abstract class MongoResource implements Resource, BlockingResource {

    protected MongoResource parent;
    protected String id;
    protected DBObject dbObject;
    protected static final String MONGO_ID_FIELD = "_id";
    protected static final String MBAAS_ID_FIELD = "id";

    public MongoResource(MongoResource parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    public MongoResource(MongoResource parent, DBObject dbObject) {
        this.parent = parent;
        this.dbObject = dbObject;
        if ( dbObject == null ) {
            new Exception().printStackTrace();
        }
    }

    protected DBObject dbObject() {
        return this.dbObject;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        if (id != null) {
            return this.id;
        }
        else {
            return (String) this.dbObject.get(MONGO_ID_FIELD);
        }
    }

    protected DB getDB() {
        return parent.getDB();
    }
}
