/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.launcher.config;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigResource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MongoLauncherConfigResource implements ConfigResource, RootResource {

    private String id;
    private Resource parent;

    private String pidFilePath;
    private Integer port = 27017;
    private String mongodPath;
    private String dbPath;
    private String logPath;
    private Boolean useAnyMongod = true;
    private String enabled = "auto";

    public MongoLauncherConfigResource(String id) {
        this.id = id;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    public String mongodPath() {
        return mongodPath;
    }

    public int port() {
        return port;
    }

    public String dbPath() {
        return dbPath;
    }

    public String logPath() {
        return logPath;
    }

    public String pidFilePath() {
        return pidFilePath;
    }

    public boolean useAnyMongod() {
        return useAnyMongod;
    }

    public String enabled() {
        return enabled;
    }

    @Override
    public void readConfigProperties(RequestContext ctx, PropertySink sink, Resource resource) throws Exception {
        sink.accept("mongodPath", mongodPath);
        sink.accept("port", port );
        sink.accept("dbPath", dbPath);
        sink.accept("logPath", logPath);
        sink.accept("pidFilePath", pidFilePath);
        sink.accept("useAnyMogod", useAnyMongod);
        sink.accept("enabled", enabled);
    }

    @Override
    public void updateConfigProperties(RequestContext ctx, ResourceState state, Responder responder, Resource resource) throws Exception {
        mongodPath = getStringProperty("mongodPath", state);
        dbPath = getStringProperty("dbPath", state);
        logPath = getStringProperty("logPath", state);
        pidFilePath = getStringProperty("pidFilePath", state);
        port = getIntegerProperty("port", state);
        if (port == null) {
            port = 27017;
        }
        useAnyMongod = getBooleanProperty("useAnyMongod", state);
        if (useAnyMongod == null) {
            useAnyMongod = true;
        }
        enabled = getStringProperty("enabled", state);
        if (enabled == null) {
            enabled = "auto";
        }
    }

    // TODO: this should be part of ResourceState probably
    private Integer getIntegerProperty(String name, ResourceState state) {
        Object val = state.getProperty(name);
        if (val == null) {
            return null;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        } else if (val instanceof String) {
            return Integer.parseInt((String) val);
        } else {
            throw new IllegalArgumentException("Invalid value of '" + name + "' property (not an integer): " + val);
        }
    }

    private Boolean getBooleanProperty(String name, ResourceState state) {
        Object val = state.getProperty(name);
        if (val == null) {
            return null;
        }
        if (val instanceof Boolean) {
            return ((Boolean) val).booleanValue();
        } else if (val instanceof String) {
            return Boolean.parseBoolean((String) val);
        } else {
            throw new IllegalArgumentException("Invalid value of '" + name + "' property (not a boolean): " + val);
        }
    }

    private String getStringProperty(String name, ResourceState state) {
        Object val = state.getProperty(name);
        if (val != null) {
            return String.valueOf(val);
        } else {
            return null;
        }
    }
}