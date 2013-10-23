package org.projectodd.restafari.mongo;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
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
        DBCollection c = this.parent.getDB().getCollection( this.collectionName );
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

//        BasicDBObject basicDBObject = new BasicDBObject();
//        if (state.id() != null) {
//            try  {
//             basicDBObject.append("_id", state.id());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

//        if (state instanceof ObjectResourceState) {
//            ObjectResourceState ors = (ObjectResourceState) state;
//
//            List<? extends PropertyResourceState> resources = ors.members().collect(Collectors.toList());
//            for (PropertyResourceState resource : resources) {
//                basicDBObject.append(resource.id(), resource.value());
//            }
//        }


        BasicDBObject basicDBObject = null;
        try {
            basicDBObject = (BasicDBObject)getObject(state);
            dbCollection.insert(basicDBObject);
        } catch (Exception e ) {
            e.printStackTrace();
        }


        responder.resourceCreated(new MongoObjectResource(this, basicDBObject));
    }

    public String toString() {
        return "[MongoCollectionResource: id=" + id() + "]";
    }

//    protected BasicDBObject createDBObject(BasicDBObject dbObject, ResourceState state) {
//        if (state.id() != null) {
//            dbObject.append("_id", state.id());
//        }
//
//        if (state instanceof CollectionResourceState) {
//            //dbObject.append("_id", state.id());
//            CollectionResourceState collectionResourceState = (CollectionResourceState) state;
//            List<? extends ResourceState> resourceStates = collectionResourceState.members().collect(Collectors.toList());
//            for (ResourceState resourceState: resourceStates) {
//                return createDBObject(dbObject, resourceState);
//            }
//        } else if (state instanceof ObjectResourceState) {
//            // add the id
//            //dbObject.append("_id", state.id());
//
//            // add the properties
//            ObjectResourceState objectResourceState = (ObjectResourceState) state;
//            List<? extends PropertyResourceState> resourceStates = objectResourceState.members().collect(Collectors.toList());
//            for (PropertyResourceState resourceState : resourceStates) {
//                if (resourceState.value() instanceof String)
//                {
//                    dbObject.append(resourceState.id(), resourceState.value());
//                }
//                else if (resourceState.value() instanceof CollectionResourceState) {
//
//                }
//            }
//        } else if (state instanceof BinaryResourceState) {
//
//        }
//
//        return dbObject;
//    }
//
//    protected BasicDBObject createObject (ResourceState resourceState) {
//        BasicDBObject basicDBObject = new BasicDBObject();
//
//        // if the state already has an id set, use it here. Otherwise one will be autocreated on insert
//        String rid = resourceState.id();
//        if (rid != null)  {
//            basicDBObject.append("_id", rid);
//        }
//
//        if (resourceState instanceof CollectionResourceState) {
//
//            ArrayList resourcesArray = new ArrayList();
//
//            CollectionResourceState collectionResourceState = (CollectionResourceState)resourceState;
//            List<? extends ResourceState> resourceStates = collectionResourceState.members().collect(Collectors.toList());
//            for (ResourceState state: resourceStates) {
//                 //resourcesArray.add(state.)
//            }
//
//            basicDBObject.append(resourceState.id(), resourcesArray);
//
//        } else if (resourceState instanceof ObjectResourceState) {
//            ObjectResourceState objectResourceState = (ObjectResourceState) resourceState;
//            List<? extends PropertyResourceState> resourceStates = objectResourceState.members().collect(Collectors.toList());
//
//            for (PropertyResourceState propertyResourceState: resourceStates) {
//                if (propertyResourceState.value() instanceof ResourceState) {
//                    basicDBObject.append(propertyResourceState.id(), createObject((ResourceState)(propertyResourceState.value())));
//                } else {
//                    basicDBObject.append(propertyResourceState.id(), propertyResourceState.value());
//                }
//            }
//        }
//
//        return basicDBObject;
//    }

    protected Object getObject (ResourceState resourceState) {
        if (resourceState instanceof PropertyResourceState) {
            PropertyResourceState pRS = (PropertyResourceState) resourceState;
            if (pRS.value() instanceof ResourceState) {
                return getObject((ResourceState)pRS.value());
            } else {
                return pRS.value();
            }
        } else {
            BasicDBObject basicDBObject = new BasicDBObject();
            // if the state already has an id set, use it here. Otherwise one will be autocreated on insert
            String rid = resourceState.id();
            if (rid != null)  {
                basicDBObject.append("_id", rid);
            }

            if (resourceState instanceof CollectionResourceState) {
                CollectionResourceState collectionResourceState = (CollectionResourceState) resourceState;
                List<? extends ResourceState> resourceStates = collectionResourceState.members().collect(Collectors.toList());
                ArrayList resourceList = new ArrayList();
                for (ResourceState state: resourceStates) {
                    resourceList.add(getObject(state));
                }

                //basicDBObject.append(collectionResourceState.id(), resourceList);
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
