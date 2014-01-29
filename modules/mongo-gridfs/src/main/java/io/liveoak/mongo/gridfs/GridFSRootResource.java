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
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigMappingExporter;
import io.liveoak.spi.resource.config.ConfigProperty;
import io.liveoak.spi.resource.config.Configurable;
import io.liveoak.spi.state.ResourceState;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
@Configurable
public class GridFSRootResource extends GridFSDirectoryResource implements RootResource {


    private MongoClient mongo;
    private DB db;
    private File tempDir;
    private Vertx vertx;

    public GridFSRootResource(String id) {
        super(null, null, id, null);
    }

    public Vertx vertx() {
        return this.vertx;
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        this.vertx = context.vertx();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        // here id always has the value of userspace which we use to map to appropriate db collection name
        responder.resourceRead(
                new GridFSUserspaceResource(ctx, this, id, new GridFSResourcePath(ctx.resourcePath()).top(2)));
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

    protected void updateConfiguration(@ConfigProperty("host") String host, @ConfigProperty("port") Integer port, @ConfigProperty("db") String dbName,
                                       @ConfigProperty("temp.dir") String rootStr) throws Exception {
        if (host == null) {
            host = "localhost";
        } else if (host.isEmpty()) {
            throw new InitializationException("Configuration value for 'host' invalid. Requires a string value. Received : " + host);
        }

        if (port == null) {
            port = 27017;
        }

        if (dbName == null || dbName.isEmpty()) {
            throw new InitializationException("String configuration value required for 'db'");
        }

        MongoClient mongo = new MongoClient(host, port);
        DB db = mongo.getDB(dbName);
        if (db == null) {
            throw new InitializationException("Unknown database " + dbName);
        }

        configure(mongo, db);


        if (rootStr != null && rootStr instanceof String == false) {
            throw new InitializationException("Configuration value for 'temp.dir' is invalid. Requires a string value. Received : " + rootStr);
        }

        if (rootStr == null) {
            rootStr = System.getProperty("java.io.tmpdir");
            System.out.println("WARN: no temp.dir specified, using: " + rootStr);
        }


        this.tempDir = new File((String) rootStr);

        if (!this.tempDir.isDirectory()) {
            if (!this.tempDir.mkdirs())
                throw new InitializationException("Failed to create temp files root: " + this.tempDir.getAbsolutePath());
        }
    }

    @ConfigMappingExporter
    public void exportConfig(HashMap<String, Object> config) {
        config.put("host", mongoClient().getAddress().getHost());
        config.put("port", mongoClient().getAddress().getPort());
        config.put("db", getDB().getName());
        config.put("temp.dir", this.tempDir.getAbsolutePath());
    }

    protected void configure(MongoClient mongo, DB db) {
        MongoClient oldMongo = this.mongo;
        this.mongo = mongo;
        this.db = db;

        if (oldMongo != null) {
            oldMongo.close();
        }
    }

    protected DB getDB() {
        return this.db;
    }

    protected MongoClient mongoClient() {
        return this.mongo;
    }

    protected File tempDir() {
        return tempDir;
    }

    @Override
    public String toString() {
        return "[GridFSRootResource: id=" + this.id() + ", path=" + path() + "]";
    }
}
