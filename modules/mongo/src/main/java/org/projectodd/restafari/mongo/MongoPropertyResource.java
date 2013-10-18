package org.projectodd.restafari.mongo;

import org.projectodd.restafari.spi.resource.async.PropertyResource;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.Responder;

/**
 * @author Bob McWhirter
 */
public class MongoPropertyResource implements PropertyResource {

    public MongoPropertyResource(MongoObjectResource object, String name) {
        this.object = object;
        this.name = name;
    }

    @Override
    public void set(Object value) {
        this.object.dbObject().put( this.name, value );
    }

    @Override
    public Object get() {
        return this.object.dbObject().get( this.name);
    }

    @Override
    public Resource parent() {
        return this.object;
    }

    @Override
    public String id() {
        return this.name;
    }

    @Override
    public void read(String id, Responder responder) {
        responder.noSuchResource( id );
    }

    @Override
    public void delete(Responder responder) {
        this.object.dbObject().removeField( this.name );
        responder.resourceDeleted( this );
    }

    private MongoObjectResource object;
    private String name;
}
