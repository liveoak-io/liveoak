package org.projectodd.restafari.vertx.resource;

import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.async.Resource;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractVertxResource implements Resource {

    public AbstractVertxResource(String id) {
        this(null, id, null );
    }

    public AbstractVertxResource(Resource parent, String id, Vertx vertx) {
        this.parent = parent;
        this.vertx = vertx;
        this.id = id;
    }

    public Vertx vertx() {
        return this.vertx;
    }

    public void vertx(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private String id;
    private Vertx vertx;
    private Resource parent;
}
