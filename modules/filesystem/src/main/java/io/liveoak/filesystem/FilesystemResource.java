/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem;

import java.io.File;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.config.ConfigMapping;
import io.liveoak.spi.resource.config.ConfigMappingExporter;
import io.liveoak.spi.resource.config.ConfigProperty;
import io.liveoak.spi.resource.config.Configurable;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
@Configurable
public class FilesystemResource extends DirectoryResource implements RootResource, FSResource {

    public FilesystemResource(String id) {
        super(null, null);
        this.id = id;
    }

    @Override
    public Vertx vertx() {
        return this.vertx;
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        this.vertx = context.vertx();
    }

    @Override
    public String id() {
        return this.id;
    }

    @ConfigMapping(@ConfigProperty("root"))
    private void updateConfig(Object... configValues) throws InitializationException {
        String rootStr = configValues[0].toString();

        if (rootStr == null) {
            throw new InitializationException("no filesystem root specified");
        }

        File file = new File(rootStr);

        if (!file.canRead()) {
            throw new InitializationException("unable to readMember filesystem at: " + file.getAbsolutePath());
        }

        this.file(file);
    }

    @ConfigMappingExporter("root")
    public Object getPath() {
        return this.file().getAbsolutePath();
    }

    public String toString() {
        return "[FilesystemResource: root=" + this.file.getAbsolutePath() + "]";
    }

    private String id;
    private Vertx vertx;

}
