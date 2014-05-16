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

import static io.liveoak.mongo.launcher.config.Constants.*;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MongoLauncherConfigResource implements ConfigResource, RootResource {

    private String id;
    private Resource parent;

    private String pidFilePath;
    private String host = "localhost";
    private Integer port = 27017;
    private String mongodPath;
    private String dbPath;
    private String logPath;
    private Boolean useAnyMongod = true;
    private Boolean useSmallFiles = true;
    private String enabled = "auto";
    private String extraArgs;

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

    public String host() {
        return host;
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

    public boolean useSmallFiles() {
        return useSmallFiles;
    }

    public String extraArgs() {
        return extraArgs;
    }

    @Override
    public void readConfigProperties(RequestContext ctx, PropertySink sink, Resource resource) throws Exception {
        sink.accept(MONGOD_PATH, mongodPath);
        sink.accept(HOST, host);
        sink.accept(PORT, port );
        sink.accept(DB_PATH, dbPath);
        sink.accept(LOG_PATH, logPath);
        sink.accept(PID_FILE_PATH, pidFilePath);
        sink.accept(USE_ANY_MONGOD, useAnyMongod);
        sink.accept(USE_SMALL_FILES, useSmallFiles);
        sink.accept(ENABLED, enabled);
        sink.accept(EXTRA_ARGS, extraArgs);
    }

    @Override
    public void updateConfigProperties(RequestContext ctx, ResourceState state, Responder responder, Resource resource) throws Exception {
        mongodPath = getStringProperty(MONGOD_PATH, state);
        dbPath = getStringProperty(DB_PATH, state);
        logPath = getStringProperty(LOG_PATH, state);
        pidFilePath = getStringProperty(PID_FILE_PATH, state);
        extraArgs = getStringProperty(EXTRA_ARGS, state);

        String host = getStringProperty(HOST, state);
        this.host = host == null ? "localhost" : host;

        Integer port = getIntegerProperty(PORT, state);
        this.port = port == null ? 27017 : port;

        Boolean useAnyMongod = getBooleanProperty(USE_ANY_MONGOD, state);
        this.useAnyMongod = useAnyMongod == null ? true : useAnyMongod;

        Boolean useSmallFiles = getBooleanProperty(USE_SMALL_FILES, state);
        this.useSmallFiles = useSmallFiles == null ? true : useSmallFiles;

        String enabled = getStringProperty(ENABLED, state);
        this.enabled = enabled == null ? "auto" : enabled;
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