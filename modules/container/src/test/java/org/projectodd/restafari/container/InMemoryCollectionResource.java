package org.projectodd.restafari.container;

import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.*;
import org.projectodd.restafari.spi.state.CollectionResourceState;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class InMemoryCollectionResource implements CollectionResource {

    public InMemoryCollectionResource(InMemoryCollectionResource parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void read(String id, Responder responder) {
        if (this.collection.containsKey(id)) {
            responder.resourceRead(this.collection.get(id));
        } else {
            System.err.println( "no-such: " + id + " to " + responder);
            responder.noSuchResource( id );
        }
    }

    @Override
    public void read(Pagination pagination, Responder responder) {
        if (pagination.getOffset() > 0 || pagination.getLimit() > 0) {
            Stream<? extends Resource> members = this.collection.values().stream().substream(pagination.getOffset()).limit(pagination.getLimit());
            responder.resourceRead(new SimplePaginatedCollectionResource<CollectionResource>(this, pagination, members));
        } else {
            responder.resourceRead(this);
        }
    }

    @Override
    public void create(ResourceState state, Responder responder) {
        String id = state.id();
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        if (state instanceof ObjectResourceState) {
            ObjectResource r = new InMemoryObjectResource(this, id, (ObjectResourceState) state);
            this.collection.put(id, r);
            responder.resourceCreated(r);
        } else if (state instanceof CollectionResourceState) {
            CollectionResource r = new InMemoryCollectionResource(this, id);
            this.collection.put(id, r);
            responder.resourceCreated(r);
        }
    }

    @Override
    public void writeMembers(ResourceSink sink) {
        this.collection.values().stream().forEach((m) -> {
            sink.accept( m );
        });
        sink.close();
    }

    @Override
    public void delete(Responder responder) {
        parent.delete(this.id);
        responder.resourceDeleted(this);
    }

    void delete(String id) {
        this.collection.remove(id);
    }

    private InMemoryCollectionResource parent;
    private String id;
    private Map<String, Resource> collection = new HashMap<>();
}
