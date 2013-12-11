/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.VertxResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.vertx.java.core.Vertx;

import static io.liveoak.filesystem.FilesystemResource.FILE_SYSTEM_LOGGER;

/**
 * @author Bob McWhirter
 */
public class DirectoryResource implements FSResource {

    public DirectoryResource(VertxResource parent, File file) {
        this(parent, file, false);
    }

    public DirectoryResource(VertxResource parent, File file, boolean writable) {
        this.parent = parent;
        this.file = file;
        this.writable = writable;
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
    public boolean writable() {
        return writable;
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        FILE_SYSTEM_LOGGER.debugf("Reading members: " + file.getPath());
        vertx().fileSystem().readDir(this.file.getPath(), (result) -> {
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
                    FSResource resource;
                    if (each.isDirectory()) {
                        resource = createDirectoryResource(each);
                    } else {
                        resource = createFileResource(each);
                    }

                    // Allow subclasses to return null so certain directories or files are not included
                    if (resource != null) {
                        sink.accept(resource);
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
        File path = new File(this.file, id);
        vertx().fileSystem().exists(path.getPath(), (existResult) -> {
            if (existResult.succeeded() && existResult.result()) {
                FSResource resource;
                if (path.isDirectory()) {
                    resource = createDirectoryResource(path);
                } else {
                    resource = createFileResource(path);
                }

                // Allow subclasses to return null so certain directories or files are not included
                if (resource != null) {
                    responder.resourceRead(resource);
                } else {
                    responder.noSuchResource(id);
                }
            } else {
                responder.noSuchResource(id);
            }
        });
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) {
        String name = state.id();
        if (name == null) {
            // Supports blank posts
            name = UUID.randomUUID().toString();
        }
        boolean createFile = "file".equals(state.getProperty("type"));
        File file = new File(this.file, name);
        FSResource resource = (createFile) ? createFileResource(file) : createDirectoryResource(file);
        if (!writable()) {
            responder.createNotSupported(resource);
        } else {
            vertx().fileSystem().exists(file.getPath(), existsResult -> {
                if (existsResult.succeeded()) {
                    boolean exists = existsResult.result();
                    if (exists) {
                        responder.resourceAlreadyExists(file.getName());
                    } else {
                        if (createFile) {
                            vertx().fileSystem().createFile(file.getPath(), result -> {
                                if (result.succeeded()) {
                                    responder.resourceCreated(resource);
                                } else {
                                    responder.internalError("Could not create file " + file + ". " + result.cause().getMessage());
                                }
                            });
                        } else {
                            vertx().fileSystem().mkdir(file.getPath(), result -> {
                                if (result.succeeded()) {
                                    responder.resourceCreated(resource);
                                } else {
                                    responder.internalError("Could not create directory " + file + ". " + result.cause().getMessage());
                                }
                            });
                        }
                    }
                } else {
                    responder.internalError("Issue checking if file " + file + " existed. " + existsResult.cause().getMessage());
                }
            });
        }
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        if (!writable()) {
            responder.deleteNotSupported(this);
        } else {
            boolean recursive = ctx.resourceParams().booleanValue("recursive", false);
            vertx().fileSystem().exists(file.getPath(), existsResult -> {
                if (existsResult.succeeded() && existsResult.result()) {
                    vertx().fileSystem().delete(file.getPath(), recursive, result -> {
                        if (result.succeeded()) {
                            responder.resourceDeleted(this);
                        } else {
                            responder.internalError(result.cause().getMessage());
                        }
                    });
                } else {
                    responder.noSuchResource(id());
                }
            });
        }
    }

    /**
     * Creates a directory structure for a given path
     *
     * @param path the path of the directory
     * @return the DirectoryResource representing the directory, or null to be excluded as a member of this resource.
     */
    protected DirectoryResource createDirectoryResource(File path) {
        return new DirectoryResource(this, path, writable());
    }

    /**
     * Creates a file structure for a given path
     *
     * @param file the file
     * @return the FileResource representing the file, or null to be excluded as a member of this resource.
     */
    protected FileResource createFileResource(File file) {
        return new FileResource(this, file, writable());
    }

    public String toString() {
        return "[DirectoryResource: file=" + this.file.getAbsolutePath() + "]";
    }

    private VertxResource parent;
    protected File file;
    private boolean writable;

    //TODO: responder.internalError should handle Exceptions
}
