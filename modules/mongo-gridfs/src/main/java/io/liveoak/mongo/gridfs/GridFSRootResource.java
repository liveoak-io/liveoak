/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.io.File;
import java.util.HashMap;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.liveoak.mongo.config.RootMongoConfigResource;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigMappingExporter;
import io.liveoak.spi.resource.config.ConfigProperty;
import io.liveoak.spi.resource.config.Configurable;
import io.liveoak.spi.state.ResourceState;
import org.vertx.java.core.Vertx;

import java.io.File;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
@Configurable
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
    public void readMember(RequestContext ctx, String id, Responder responder) {
        // here id always has the value of userspace which we use to map to appropriate db collection name
        System.err.println( " === " + ctx.resourcePath() );
        responder.resourceRead(
                new GridFSUserspaceResource(ctx, this, id, new GridFSResourcePath(ctx.resourcePath()).top(3)));
    }


    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        sink.close();
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("name", "GridFS");
        sink.accept("version", "1.0");
        sink.close();
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
