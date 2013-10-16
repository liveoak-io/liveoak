package org.projectodd.restafari.mongo;

import com.mongodb.DBObject;
import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.state.ObjectResourceState;

import java.util.stream.Stream;

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
    }

    DBObject dbObject() {
        return this.dbObject;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    public String id() {
        return this.dbObject.get(ID_FIELD).toString();
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
        responder.resourceUpdated( this );
    }

    @Override
    public void delete(Responder responder) {
        // TODO
    }

    @Override
    public void writeMembers(ResourceSink sink) {
        this.dbObject.keySet().stream().forEach((name) -> {
            sink.accept( new MongoPropertyResource(this, name) );
        });
        sink.close();
    }
}
