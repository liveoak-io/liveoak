/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem;

import java.io.File;
import java.util.HashMap;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.config.ConfigMappingExporter;
import io.liveoak.spi.resource.config.ConfigProperty;
import io.liveoak.spi.resource.config.Configurable;
import org.vertx.java.core.Vertx;

import java.io.File;

/**
 * @author Bob McWhirter
 */
@Configurable
public class FilesystemResource extends DirectoryResource implements RootResource, FSResource {

    public FilesystemResource(FileSystemAdminResource adminResource, String id, Vertx vertx) {
        super(null, null);
        this.id = id;
        this.vertx = vertx;
        this.adminResource = adminResource;
    }


    @Override
    public File file() {
        return this.adminResource.directory();
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Vertx vertx() {
        return this.vertx;
    }

    @Override
    public String id() {
        return this.id;
    }

    public String toString() {
        return "[FilesystemResource: dir=" + this.adminResource.directory() + "]";
    }

    private final FileSystemAdminResource adminResource;
    private Resource parent;
    private String id;
    private Vertx vertx;

}
