package org.projectodd.restafari.spi.synch;

import org.projectodd.restafari.spi.PaginatedCollectionResource;
import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public interface SynchronousCollectionResource extends SynchronousResource {

    Stream<Resource> members();

    Resource create(ResourceState state) throws Exception;

    PaginatedCollectionResource readPage(Pagination pagination);
}
