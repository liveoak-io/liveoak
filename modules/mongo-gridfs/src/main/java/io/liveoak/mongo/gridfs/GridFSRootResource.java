/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.DB;
import io.liveoak.mongo.config.RootMongoConfigResource;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GridFSRootResource extends GridFSDirectoryResource implements RootResource {

    private Resource parent;

    private File tempDir;
    private Vertx vertx;
    private final RootMongoConfigResource mongoConfigResource;

    public GridFSRootResource(String id, RootMongoConfigResource configResource, File tempDir, Vertx vertx) {
        super(null, null, id, null);
        this.tempDir = tempDir;
        this.vertx = vertx;
        this.mongoConfigResource = configResource;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    public Vertx vertx() {
        return this.vertx;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, String> result = new HashMap<>();
        result.put("name", "GridFS");
        result.put("version", "1.0");
        return result;
    }

    @Override
    public Resource member(RequestContext ctx, String id) {
        // here id always has the value of userspace which we use to map to appropriate db collection name
        return new GridFSUserspaceResource(ctx, this, id, new GridFSResourcePath(ctx.resourcePath()).top(3));
    }

    @Override
    public Collection<Resource> members(RequestContext ctx) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) {
        responder.createNotSupported(this);
    }

    protected DB getDB() {
        return this.mongoConfigResource.getDB();
    }

    protected File tempDir() {
        return tempDir;
    }

    @Override
    public String toString() {
        return "[GridFSRootResource: id=" + this.id() + ", path=" + path() + "]";
    }
}
