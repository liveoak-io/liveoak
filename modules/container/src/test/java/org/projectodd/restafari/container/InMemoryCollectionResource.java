package org.projectodd.restafari.container;

import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.*;
import org.projectodd.restafari.spi.state.CollectionResourceState;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.Collection;
import java.util.LinkedHashMap;
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
    public void readContent(Pagination pagination, ResourceSink sink) {
        Stream<Resource> stream = applyPagination(pagination, this.collection.values());
        stream.forEach((m) -> {
            sink.accept(m);
        });
        try {
            sink.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private Stream<Resource> applyPagination(Pagination pagination, Collection<Resource> values) {
        boolean skipPagination = pagination == null
                || pagination == Pagination.NONE
                || (pagination.getOffset() == 0 && pagination.getLimit() >= values.size());

        if (skipPagination) {
            return values.stream();
        }
        return values.stream().substream(pagination.getOffset()).limit(pagination.getLimit());
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
    private Map<String, Resource> collection = new LinkedHashMap<>();
}
