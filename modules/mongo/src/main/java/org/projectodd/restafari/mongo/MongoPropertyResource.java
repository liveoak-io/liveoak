package org.projectodd.restafari.mongo;

import com.mongodb.DBObject;
import org.projectodd.restafari.spi.resource.async.PropertyContentSink;
import org.projectodd.restafari.spi.resource.async.PropertyResource;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.Responder;

/**
 * @author Bob McWhirter
 */
public class MongoPropertyResource extends MongoResource implements PropertyResource {

    public MongoPropertyResource (MongoResource parent, String name) {
        super(parent, name);
    }

    @Override
    public Object get(RequestContext ctx) {
        Object object = this.parent.dbObject().get(this.id());

        if (object instanceof BasicDBObject) {
            return new MongoObjectResource(this, (DBObject)object);
        } else if (object instanceof ArrayList) {
            MongoCollectionResource mcr = new MongoCollectionResource(this, this.id());
            return mcr;
        }  else {
            return this.parent.dbObject().get(this.id());
        }
    }

    @Override
    public void set(Object value) {
        this.parent.dbObject().put(this.id(), value);
    }

    @Override
    public void readContent(RequestContext ctx, PropertyContentSink sink) {
        sink.accept(get());
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.name;
    }

    @Override
    public void read(RequestContext ctx, String id, Responder responder) {
        responder.noSuchResource( id );
    }

    @Override
    public void delete(RequestContext ctx,Responder responder) {
        this.parent.dbObject().removeField( this.id() );
        responder.resourceDeleted( this );
    }

    public String toString() {
        return "[MongoProperty: obj=" + this.parent.dbObject() + "; name=" + this.id() + "]";
    }
}
