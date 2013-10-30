package org.projectodd.restafari.mongo;

import com.mongodb.DBObject;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.async.PropertyContentSink;
import org.projectodd.restafari.spi.resource.async.PropertyResource;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.Responder;

/**
 * @author Bob McWhirter
 */
public class MongoPropertyResource implements PropertyResource {

	//TODO: fix issues with mongo resource parents
    protected MongoObjectResource parent;
    protected String name;

    public MongoPropertyResource (MongoObjectResource parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public Object get(RequestContext ctx) {
        Object object = this.parent.dbObject().get(this.name);

        if (object instanceof DBObject) {
            return new MongoObjectResource(null, (DBObject)object);
        }

        return this.parent.dbObject().get(this.name);
    }

    @Override
    public void set(Object value) {
        this.parent.dbObject().put(this.name, value);
    }

    @Override
    public void readContent(RequestContext ctx, PropertyContentSink sink) {
        sink.accept(get(ctx));
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
    public void delete(RequestContext ctx, Responder responder) {
        this.parent.dbObject().removeField( this.name );
        responder.resourceDeleted( this );
    }

    public String toString() {
        return "[MongoProperty: obj=" + this.parent.dbObject() + "; name=" + this.name + "]";
    }
}
