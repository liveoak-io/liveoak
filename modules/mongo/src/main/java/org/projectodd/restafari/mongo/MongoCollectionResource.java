package org.projectodd.restafari.mongo;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
class MongoCollectionResource implements CollectionResource {

    private MongoDBResource parent;
    private String collectionName;

    MongoCollectionResource(MongoDBResource parent, String collectionName) {
        this.parent = parent;
        this.collectionName = collectionName;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.collectionName;
    }

    @Override
    public void read(String id, Responder responder) {
        DBObject dbObject = this.parent.getDB().getCollection(this.collectionName).findOne(new BasicDBObject("_id", new ObjectId(id)));
        if (dbObject == null) {
            responder.noSuchResource(id);
        }

        responder.resourceRead(new MongoObjectResource(this, dbObject));
    }

    @Override
    public void delete(Responder responder) {
        responder.deleteNotSupported(this);
    }

    DB getDB() {
        return this.parent.getDB();
    }

    @Override
    public void read(Pagination pagination, Responder responder) {
        if (pagination.getLimit() > 0 || pagination.getOffset() > 0) {
            responder.resourceRead(new PaginatedMongoCollectionResource(this, pagination));
        } else {
            responder.resourceRead(this);
        }
    }

    public void writeMembers(ResourceSink sink) {
        DBCollection c = this.parent.getDB().getCollection( this.collectionName );
        DBCursor cursor = c.find();

        cursor.forEach((e) -> {
            sink.accept(new MongoObjectResource(this, e));
        });

        sink.close();
    }

    @Override
    public void create(ResourceState state, Responder responder) {

    }

    public String toString() {
        return "[MongoCollectionResource: id=" + id() + "]";
    }
}
