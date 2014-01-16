/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GridFSResource implements Resource {

    /* Parent that will end up in response as part of the uri() */
    private final GridFSDirectoryResource parent;

    /* Id that will end up in response as id of this resource */
    private final String id;

    /* Request context representing current request being served */
    private final RequestContext ctx;

    /* Path that corresponds to this resource */
    private GridFSResourcePath path;

    /* DBObject loaded from MongoDB, and wrapped for easier handling */
    private GridFSDBObject fileInfo;

    public GridFSResource(RequestContext ctx, GridFSDirectoryResource parent, String id, GridFSDBObject fileInfo, GridFSResourcePath path) {
        this.ctx = ctx;
        this.parent = parent;
        this.id = id;
        this.fileInfo = fileInfo;
        this.path = path;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    protected RequestContext requestContext() {
        return ctx;
    }

    protected GridFSDBObject fileInfo() {
        return fileInfo;
    }

    protected GridFSResourcePath path() {
        return path;
    }

    protected GridFSRootResource getRoot() {
        Resource res = this;
        while (res != null && res instanceof GridFSRootResource == false) {
            res = res.parent();
        }

        if (res instanceof GridFSRootResource) {
            return (GridFSRootResource) res;
        }
        return null;
    }

    protected GridFSBlobsDirResource getBlobsRoot() {
        Resource res = this;
        while (res != null && res instanceof GridFSBlobsDirResource == false) {
            res = res.parent();
        }

        if (res instanceof GridFSBlobsDirResource) {
            return (GridFSBlobsDirResource) res;
        }

        GridFSUserspaceResource userspace = getUserspace();
        String blobsId = ".blobs";
        return new GridFSBlobsDirResource(ctx, userspace, blobsId, userspace.path().append(blobsId));
    }

    protected GridFSFilesDirResource getFilesRoot() {
        Resource res = this;
        while (res != null && res instanceof GridFSFilesDirResource == false) {
            res = res.parent();
        }

        if (res instanceof GridFSFilesDirResource) {
            return (GridFSFilesDirResource) res;
        }
        GridFSUserspaceResource userspace = getUserspace();
        String filesId = ".files";
        return new GridFSFilesDirResource(ctx, userspace, filesId, userspace.path().append(filesId));
    }

    protected GridFSUserspaceResource getUserspace() {
        Resource res = this;
        while (res != null && res instanceof GridFSUserspaceResource == false) {
            res = res.parent();
        }

        if (res instanceof GridFSUserspaceResource) {
            return (GridFSUserspaceResource) res;
        }
        return null;
    }

    protected String getSelfUri() {
        return uri().toString();
    }

    protected String getParentUri() {
        return parent().uri().toString();
    }

    protected String getBlobUri() {
        return null;
    }
}
