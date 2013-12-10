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
public class GridFSFilesItemResource extends GridFSFileResource {

    public GridFSFilesItemResource(RequestContext ctx, GridFSDirectoryResource parent, String id, GridFSDBObject fileInfo, GridFSResourcePath path) {
        super(ctx, parent, id, fileInfo, path);
    }

    @Override
    protected String getSelfUri() {
        return getFilesRoot().uri().toString() + "/" + id();
    }

    @Override
    protected String getParentUri() {
        String parentId = fileInfo().getParentIdAsString();

        if (parentId != null) {
            return getFilesRoot().uri().toString() + "/" + parentId;
        }
        return null;
    }

    @Override
    protected String getBlobUri() {
        return getBlobsRoot().uri().toString() + "/" + id();
    }

    @Override
    public String toString() {
        return "[GridFSFilesItemResource: id=" + this.id() + ", path=" + path() + "]";
    }
}
