/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.logging.Logger;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class FilesystemResource extends DirectoryResource implements RootResource, FSResource {

    public FilesystemResource() {
        this(null);
    }

    public FilesystemResource(String id) {
        this(id, false);
    }

    public FilesystemResource(String id, boolean writable) {
        super(null, null, writable);
        this.id = id;
        this.configResource = new FilesystemConfigResource(this);
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
    public Resource configuration() {
        return this.configResource;
    }


    @Override
    public String id() {
        return this.id;
    }

    public String toString() {
        return "[FilesystemResource: root=" + this.file.getAbsolutePath() + "]";
    }

    private String id;
    private Vertx vertx;
    private FilesystemConfigResource configResource;

    private static final Logger log = Logger.getLogger("io.liveoak.filesystem"); //TODO: This could be done for us, and passed/set for each RootResource that wants one
    static Logger FILE_SYSTEM_LOGGER = log;

}
