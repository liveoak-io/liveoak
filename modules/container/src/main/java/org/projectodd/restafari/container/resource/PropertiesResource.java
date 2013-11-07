package org.projectodd.restafari.container.resource;

import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.resource.async.SimplePropertyResource;
import org.projectodd.restafari.spi.state.ObjectResourceState;

import java.util.Properties;

/**
 * @author Bob McWhirter
 */
public class PropertiesResource implements ObjectResource {

    public PropertiesResource(ContainerResource parent) {
        this.parent = parent;
    }

    @Override
    public void update(RequestContext ctx, ObjectResourceState state, Responder responder) {
        responder.updateNotSupported( this );
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        Properties allProps = System.getProperties();
        for ( String key : allProps.stringPropertyNames() ) {
            sink.accept( new SimplePropertyResource( this, key, allProps.getProperty( key ) ));
        }

        try {
            sink.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return "properties";
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        String value = System.getProperty( id );
        if ( value != null ) {
            responder.resourceRead( new SimplePropertyResource( this, id, value ));
        } else {
            responder.noSuchResource( id );
        }
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        responder.deleteNotSupported( this );
    }

    private ContainerResource parent;
}
