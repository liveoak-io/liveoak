package org.projectodd.restafari.spi.resource.sync;

import org.projectodd.restafari.spi.resource.BlockingResource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.PaginatedCollectionResource;
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

    Resource create(ResourceState state) throws Exception;

    PaginatedCollectionResource readPage(Pagination pagination);

    @Override
    default void read(Pagination pagination, Responder responder) {
        responder.resourceRead(readPage(pagination));
    }

    @Override
    default void create(ResourceState state, Responder responder) {
        try {
            Resource result = create(state);
            responder.resourceCreated(result);
        } catch (Exception e) {
            responder.createNotSupported(this);
        }
    }

    @Override
    default void writeMembers(ResourceSink sink) {
        members().forEach((m) -> {
            sink.accept( m );
        });
        sink.close();
    }
}
