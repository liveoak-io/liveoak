package org.projectodd.restafari.spi.resource.sync;

import org.projectodd.restafari.spi.CreateNotSupportedException;
import org.projectodd.restafari.spi.ResourceException;
import org.projectodd.restafari.spi.resource.BlockingResource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public interface SynchronousCollectionResource extends SynchronousResource, CollectionResource, BlockingResource {

    Stream<Resource> members();

    Resource create(ResourceState state) throws ResourceException;

    @Override
    default void create(ResourceState state, Responder responder) {
        try {
            Resource result = create(state);
            responder.resourceCreated(result);
        } catch (CreateNotSupportedException e) {
            responder.createNotSupported( this );
        } catch (Exception e) {
            // TODO be more specific
            responder.createNotSupported(this);
        }
    }

    @Override
    default void readContent(Pagination pagination, ResourceSink sink) {
        Stream<Resource> stream = members().substream( pagination.getOffset() );

        if ( pagination.getLimit() > 0 ) {
            stream = stream.limit( pagination.getLimit() );
        }
        stream.forEach((m) -> {
            sink.accept( m );
        });
        try {
            sink.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
