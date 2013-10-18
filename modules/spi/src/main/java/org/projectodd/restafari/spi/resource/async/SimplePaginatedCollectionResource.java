package org.projectodd.restafari.spi.resource.async;

import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.resource.Resource;

import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class SimplePaginatedCollectionResource<C extends CollectionResource> extends AbstractPaginatedCollectionResource<C> {

    public SimplePaginatedCollectionResource(C collection, Pagination pagination, Stream<? extends Resource> members) {
        super(collection, pagination);
        this.members = members;
    }

    @Override
    public void writeMembers(ResourceSink sink) {
        members.forEach((e) -> {
            sink.accept( e );
        } );
        sink.close();
    }

    private Stream<? extends Resource> members;
}
