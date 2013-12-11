/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.logging.config;

import java.io.File;

import io.liveoak.logging.LoggingRootResource;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.ConfigResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class LoggingConfigResource implements ConfigResource {

    public static final String LOG_DIR = "logDir";

    private final LoggingRootResource loggingResource;
    private LogDirConfig logDirConfig;

    public LoggingConfigResource(LoggingRootResource loggingResource) {
        this.loggingResource = loggingResource;
    }

    @Override
    public Resource parent() {
        return loggingResource;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(LOG_DIR, logDirConfig.toMap());
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        logDirConfig = new LogDirConfig((ResourceState) state.getProperty(LOG_DIR));

        responder.resourceUpdated(this);
    }

    public LogDirConfig logDirConfig() {
        return logDirConfig;
    }

    public String logDirName() {
        return new File(logDirConfig.path()).getName();
    }
}
