package org.projectodd.restafari.mongo;

import com.mongodb.*;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.BlockingResource;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.resource.async.SimplePropertyResource;
import org.projectodd.restafari.spi.state.ObjectResourceState;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class MongoObjectResource extends MongoResource implements ObjectResource {

    public MongoObjectResource(MongoResource parent, DBObject dbObject) {
        super(parent, dbObject);
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

//    @Override
//    public String id() {
//        Object candidateId = this.dbObject.get(MONGO_ID_FIELD);
//        if (candidateId == null) {
//            Resource parent = this.parent();
//            String id = "/";
//            while (parent.parent() != null) {
//                id = "/" + parent.parent().id()  + id;
//                parent = parent.parent();
//            }
//            return id;
//            //return this.parent().id();
//        }  else {
//            return candidateId.toString();
//        }
//
//    }

    @Override
    public String id() {
        if (this.dbObject instanceof BasicDBObject) {
        Object candidateId = this.dbObject.get(MONGO_ID_FIELD);
        if ( candidateId == null ) {
            return this.parent().id();
        }
        return candidateId.toString();
        } else if (this.dbObject instanceof BasicDBList) {
            return null;
        } else {
            return null;
        }
    }

    @Override
    public void readMember(RequestContext ctx, String childId, Responder responder) {
        Object value = this.dbObject.get(childId);
        if (value != null) {
            responder.resourceRead(new MongoPropertyResource(this, childId));
        } else {
            responder.noSuchResource(childId);
        }
    }

    @Override
    public void update(RequestContext ctx, ObjectResourceState state, Responder responder) {
        state.members().forEach((p) -> {
            if (!p.id().equals(MONGO_ID_FIELD) && !p.id().equals(MBAAS_ID_FIELD))
            {
                this.dbObject.put(p.id(), p.value());
            }
        });

        this.parent.getDB().getCollection(parent.id()).update(new BasicDBObject().append(MONGO_ID_FIELD, this.dbObject.get(MONGO_ID_FIELD)), this.dbObject);

        responder.resourceUpdated(this);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
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
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        this.dbObject.keySet().stream().forEach((name) -> {
            // the mongo internal id should never be returned to the user
            // the mbaas gets the id separately though getId when creating the object and should not be returned either
            if (!name.equals(MONGO_ID_FIELD) && !name.equals(MBAAS_ID_FIELD)) {
                Object object = this.dbObject.get(name);
                sink.accept(new MongoPropertyResource(this, name));
            }
        });
        try {
            sink.close();
        } catch (Exception e) {
            e.printStackTrace(); //TODO: properly handle errors
        }
    }

    public String toString() {
        return "[MongoObject: obj=" + this.dbObject() + "]";
    }

    @Override
    public java.net.URI uri() {
        List<String> segments = new ArrayList<>();
        Resource current = this;

        if (parent instanceof MongoPropertyResource)
        {
            current = current.parent();
        }

        while (current != null) {
            segments.add(0, current.id());
            current = current.parent();
        }

        StringBuilder buf = new StringBuilder();

        segments.forEach((s) -> {
            buf.append( "/" );
            buf.append( s );
        });

        return URI.create(buf.toString());
    }
}
