/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class DirectoryResource implements FSResource {

    public DirectoryResource(DirectoryResource parent, File file) {
        this.parent = parent;
        this.file = file;
    }

    public Vertx vertx() {
        return this.parent.vertx();
    }

    public File file() {
        return this.file;
    }

    public void file(File file) {
        this.file = file;
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        vertx().fileSystem().readDir(file().getPath(), (result) -> {
            if (result.failed()) {
                sink.close();
            } else {
                List<File> sorted = new ArrayList<>();

                for (String filename : result.result()) {
                    File child = new File(filename);
                    sorted.add(child);
                }

                sorted.sort((left, right) -> {
                    if (left.isDirectory() && right.isDirectory()) {
                        return 0;
                    }

                    if (left.isFile() && right.isFile()) {
                        return 0;
                    }

                    if (left.isDirectory()) {
                        return -1;
                    }

                    if (left.isFile()) {
                        return 1;
                    }

                    return 0;

                });

                for (File each : sorted) {
                    if (each.isDirectory()) {
                        sink.accept(createDirectoryResource(each));
                    } else {
                        sink.accept(createFileResource(each));
                    }
                }
                sink.close();
            }
        });
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.file.getName();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        File path = new File(file(), id);
        vertx().fileSystem().exists(path.getPath(), (existResult) -> {
            if (existResult.succeeded() && existResult.result()) {
                if (path.isDirectory()) {
                    responder.resourceRead(createDirectoryResource(path));
                } else {
                    responder.resourceRead(createFileResource(path));
                }
            } else {
                responder.noSuchResource(id);
            }
        });
    }

    protected DirectoryResource createDirectoryResource(File path) {
        return new DirectoryResource(this, path);
    }

    protected FileResource createFileResource(File file) {
        return new FileResource(this, file);
    }

    public String toString() {
        return "[DirectoryResource: file=" + this.file.getAbsolutePath() + "]";
    }

    private DirectoryResource parent;
    protected File file;
}
