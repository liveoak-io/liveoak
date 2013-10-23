package org.projectodd.restafari.container.resource;

import org.projectodd.restafari.spi.InitializationException;
import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.ResourceContext;
import org.projectodd.restafari.spi.resource.RootResource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ContainerResource implements CollectionResource, RootResource {


    public ContainerResource(String id) {
        this.id = id;
        this.propertiesResource = new PropertiesResource( this );
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void create(ResourceState state, Responder responder) {
        responder.createNotSupported( this );
    }

    @Override
    public void readContent(Pagination pagination, ResourceSink sink) {
        sink.accept( this.propertiesResource );
        try {
            sink.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void read(String id, Responder responder) {
        if ( id.equals( this.propertiesResource.id() ) ) {
            responder.resourceRead( this.propertiesResource );
        } else {
            responder.noSuchResource( id );
        }
    }

    @Override
    public void delete(Responder responder) {
        responder.deleteNotSupported( this );
    }

    private String id;

    private PropertiesResource propertiesResource;
}
