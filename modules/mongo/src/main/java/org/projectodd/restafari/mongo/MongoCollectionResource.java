package org.projectodd.restafari.mongo;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.state.CollectionResourceState;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.spi.state.PropertyResourceState;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Bob McWhirter
 */
//TODO: fix the situations with all the mongo resources to have a proper hierarchy with parents
//class MongoCollectionResource implements CollectionResource {
class MongoCollectionResource extends MongoDBResource {
    private MongoDBResource parent;
    private String collectionName;

    MongoCollectionResource(MongoDBResource parent, String collectionName) {
        super(collectionName);
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
            return;
        }

        responder.resourceRead(new MongoObjectResource(this, dbObject));
    }

    @Override
    public void delete(Responder responder) {
        if (getDB().collectionExists(id())) {
            getDB().getCollection(id()).drop();
            responder.resourceDeleted(this);
        } else {
            responder.noSuchResource(id());
        }
    }

    DB getDB() {
        return this.parent.getDB();
    }

    @Override
    public void readContent(Pagination pagination, ResourceSink sink) {
        DBCollection c = this.parent.getDB().getCollection(this.collectionName);
        DBCursor cursor = c.find();

        cursor.forEach((e) -> {
            sink.accept(new MongoObjectResource(this, e));
        });

        try {
            sink.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void create(ResourceState state, Responder responder) {
        DBCollection dbCollection = this.parent.getDB().getCollection(this.collectionName);

        BasicDBObject basicDBObject = null;
        try {
            basicDBObject = (BasicDBObject) getObject(state);
            dbCollection.insert(basicDBObject);
        } catch (Exception e) {
            e.printStackTrace();
        }


        responder.resourceCreated(new MongoObjectResource(this, basicDBObject));
    }

    public String toString() {
        return "[MongoCollectionResource: id=" + id() + "]";
    }

    protected Object getObject(ResourceState resourceState) {
        if (resourceState instanceof PropertyResourceState) {
            PropertyResourceState pRS = (PropertyResourceState) resourceState;
            if (pRS.value() instanceof ResourceState) {
                return getObject((ResourceState) pRS.value());
            } else {
                return pRS.value();
            }
        } else {
            BasicDBObject basicDBObject = new BasicDBObject();
            // if the state already has an id set, use it here. Otherwise one will be autocreated on insert
            String rid = resourceState.id();
            if (rid != null) {
                basicDBObject.append("_id", rid);
            }

            if (resourceState instanceof CollectionResourceState) {
                CollectionResourceState collectionResourceState = (CollectionResourceState) resourceState;
                List<? extends ResourceState> resourceStates = collectionResourceState.members().collect(Collectors.toList());
                ArrayList resourceList = new ArrayList();
                for (ResourceState state : resourceStates) {
                    resourceList.add(getObject(state));
                }

                return resourceList;
            } else if (resourceState instanceof ObjectResourceState) {
                ObjectResourceState objectResourceState = (ObjectResourceState) resourceState;
                List<? extends PropertyResourceState> resourceStates = objectResourceState.members().collect(Collectors.toList());
                for (PropertyResourceState pRS : resourceStates) {
                    basicDBObject.append(pRS.id(), getObject(pRS));
                }
            }
            return basicDBObject;
        }
    }
}
