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
public class GridFSFilesDirItemResource extends GridFSDirectoryResource {

    public GridFSFilesDirItemResource(RequestContext ctx, GridFSFilesDirResource parent, String id, GridFSResourcePath path, GridFSDBObject info) {
        super(ctx, parent, id, path, info);
    }

    @Override
    public GridFSFilesDirResource parent() {
        return (GridFSFilesDirResource) super.parent();
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
    protected GridFSDirectoryResource getChildParent() {
        return parent();
    }

    @Override
    protected GridFSDirectoryResource newChildDir(GridFSResourcePath path, GridFSDBObject item) {
        return new GridFSFilesDirItemResource(requestContext(), parent(), item.getId().toString(), path, item);
    }

    @Override
    protected GridFSResource newChildItem(GridFSDBObject item) {
        return new GridFSFilesItemResource(requestContext(), getChildParent(),
                item.getId().toString(), item, path().append(item.getId().toString()));
    }

    @Override
    public String toString() {
        return "[GridFSFilesDirItemResource: id=" + this.id() + ", path=" + path() + "]";
    }
}
