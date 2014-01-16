/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import io.liveoak.spi.RequestContext;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GridFSFilesPathItemResource extends GridFSFilesItemResource {

    public GridFSFilesPathItemResource(RequestContext ctx, GridFSFilesDirResource parent, String id, GridFSDBObject fileInfo, GridFSResourcePath path) {
        super(ctx, parent, id, fileInfo, path);
    }

    @Override
    protected String getSelfUri() {
        return path().toString() + ";meta";
    }

    @Override
    protected String getParentUri() {
        return path().parent().toString();
    }

    @Override
    protected String getBlobUri() {
        return path().toString();
    }


    @Override
    public String toString() {
        return "[GridFSFilesPathItemResource: id=" + this.id() + ", path=" + path() + "]";
    }
}
