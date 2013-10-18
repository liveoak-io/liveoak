package org.projectodd.restafari.spi.resource.async;

import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractPaginatedCollectionResource<C extends CollectionResource> implements PaginatedCollectionResource {

    public AbstractPaginatedCollectionResource(C collection, Pagination pagination) {
        this.collection = collection;
        this.pagination = pagination;
    }

    @Override
    public C collection() {
        return collection;
    }

    @Override
    public Pagination pagination() {
        return pagination;
    }

    @Override
    public Resource parent() {
        return collection.parent();
    }

    @Override
    public String id() {
        return collection.id();
    }

    @Override
    public void read(String id, Responder responder) {
        responder.readNotSupported(this);
    }

    @Override
    public void delete(Responder responder) {
        responder.deleteNotSupported(this);
    }

    @Override
    public void read(Pagination pagination, Responder responder) {
        this.collection.read(pagination, responder);
    }

    @Override
    public void create(ResourceState state, Responder responder) {
        this.collection.create(state, responder);
    }

    private C collection;
    private Pagination pagination;
}
