/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.logging;

import io.liveoak.logging.config.LogDirConfig;
import io.liveoak.logging.config.LoggingConfigResource;
import io.liveoak.logging.fs.LogDirectoryResource;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.VertxResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import org.jboss.logging.Logger;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class LoggingRootResource implements RootResource, LoggingResource, VertxResource {

    private Logger log;
    private Vertx vertx;
    private final String id;
    private final LoggingConfigResource config;

    public LoggingRootResource() {
        this("logging");
    }

    public LoggingRootResource(String id) {
        this.id = id;
        this.config = new LoggingConfigResource(this);
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        LogDirConfig logDir = config.logDirConfig();
        sink.accept(new LogDirectoryResource(this, logDir));
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        if (id.equals(config.logDirName())) {
            responder.resourceRead(new LogDirectoryResource(this, config.logDirConfig()));
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        log = context.container().logger();
        vertx = context.vertx();
    }

    @Override
    public Resource configuration() {
        return config;
    }

    @Override
    public Logger logger() {
        return log;
    }

    @Override
    public Vertx vertx() {
        return vertx;
    }
}
