package org.projectodd.restafari.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.resource.async.SimplePropertyResource;
import org.projectodd.restafari.spi.state.ObjectResourceState;

/**
 * @author Bob McWhirter
 */
public class MongoObjectResource implements ObjectResource {

    private static final String ID_FIELD = "_id";

    private MongoCollectionResource parent;
    private final DBObject dbObject;

    public MongoObjectResource(MongoCollectionResource parent, DBObject dbObject) {
        this.parent = parent;
        this.dbObject = dbObject;
        if ( dbObject == null ) {
            new Exception().printStackTrace();
        }
    }

    DBObject dbObject() {
        return this.dbObject;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    public String id() {
        Object candidateId = this.dbObject.get(ID_FIELD);
        if ( candidateId == null ) {
            return null;
        }
        return candidateId.toString();
    }

    @Override
    public void read(String id, Responder responder) {
        Object value = this.dbObject.get(id);
        if (value != null) {
            responder.resourceRead(new SimplePropertyResource(this, id, value));
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public void update(ObjectResourceState state, Responder responder) {
        state.members().forEach((p) -> {
            this.dbObject.put(p.id(), p.value());
        });

        this.parent.getDB().getCollection(parent.id()).update(new BasicDBObject().append("_id", this.dbObject.get("_id")), this.dbObject);

        responder.resourceUpdated(this);
    }

    @Override
    public void delete(Responder responder) {
        DB db = parent.getDB();
        if (db.collectionExists(parent.id()))
        {
          DBCollection dbCollection = db.getCollection(parent.id());
          DBObject dbObject = dbCollection.findOne(this.dbObject);
          if (dbObject != null)
          {
            dbCollection.remove(dbObject);
            responder.resourceDeleted(this);
          }
        }

        responder.noSuchResource(id());
    }

    @Override
    public void readContent(ResourceSink sink) {
        this.dbObject.keySet().stream().forEach((name) -> {
            // the _id field is handled in the special case in id()
            if (!name.equals(ID_FIELD)) {
                System.err.println( "SINK: " + name );
                sink.accept( new MongoPropertyResource(this, name) );
            }
        });
        try {
            sink.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public String toString() {
        return "[MongoObject: obj=" + this.dbObject() + "]";
    }
}
