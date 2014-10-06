/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.spi.LiveOak;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class InMemoryCollectionResource implements SynchronousResource {

    public InMemoryCollectionResource(Resource parent, String id) {
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
    public Resource member(RequestContext ctx, String id) {
        return this.collection.get(id);
    }

    @Override
    public Collection<Resource> members(RequestContext ctx) {
        Stream<Resource> stream = applyPagination(ctx.pagination(), this.collection.values());
        return stream.collect(Collectors.toList());
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) {
        String id = state.id();
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        if (state.getProperty("type") != null && state.getProperty("type").equals("collection")) {
            Resource r = new InMemoryCollectionResource(this, id);
            this.collection.put(id, r);
            responder.resourceCreated(r);
        } else if (state instanceof ResourceState) {
            Resource r = new InMemoryObjectResource(this, id, cleanse(state));
            this.collection.put(id, r);
            responder.resourceCreated(r);
        }
    }

    protected ResourceState cleanse(ResourceState state) {
        state.removeProperty(LiveOak.ID);
        state.removeProperty(LiveOak.SELF);
        return state;
    }

    private Stream<Resource> applyPagination(Pagination pagination, Collection<Resource> values) {
        boolean skipPagination = pagination == null
                || pagination == Pagination.NONE
                || (pagination.offset() == 0 && pagination.limit() >= values.size());

        if (skipPagination) {
            return values.stream();
        }
        return values.stream().skip(pagination.offset()).limit(pagination.limit());
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        if ( parent instanceof InMemoryCollectionResource ) {
            ((InMemoryCollectionResource)parent).delete(this.id);
        }
        responder.resourceDeleted(this);
    }

    void delete(String id) {
        this.collection.remove(id);
    }

    public void addMember(Resource member) {
        this.collection.put(member.id(), member);
    }

    protected Resource parent;
    private String id;
    private Map<String, Resource> collection = new LinkedHashMap<>();
}
