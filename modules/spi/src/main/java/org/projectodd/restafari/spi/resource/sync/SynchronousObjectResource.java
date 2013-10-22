package org.projectodd.restafari.spi.resource.sync;

import org.projectodd.restafari.spi.ResourceException;
import org.projectodd.restafari.spi.UpdateNotSupportedException;
import org.projectodd.restafari.spi.resource.BlockingResource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;
import org.projectodd.restafari.spi.resource.async.PropertyResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.state.ObjectResourceState;

import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public interface SynchronousObjectResource extends SynchronousResource, ObjectResource, BlockingResource {
    void update(ObjectResourceState state) throws ResourceException;
    Stream<PropertyResource> members();

    @Override
    default void update(ObjectResourceState state, Responder responder) {
        try {
            update(state);
            responder.resourceUpdated(this);
        } catch (UpdateNotSupportedException e) {
            responder.updateNotSupported( this );
        } catch (Exception e) {
            // TODO be more specific
            responder.updateNotSupported(this);
        }
    }

    @Override
    default void readContent(ResourceSink sink) {
        members().forEach((m) -> {
            sink.accept( m );
        });
        try {
            sink.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
