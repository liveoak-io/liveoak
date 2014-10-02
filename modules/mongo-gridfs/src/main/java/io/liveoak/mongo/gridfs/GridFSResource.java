/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.mongodb.DBObject;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.bson.types.ObjectId;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GridFSResource implements SynchronousResource {

    protected static Set<String> FILTERED = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            new String[]{"aliases", "chunkSize", "_id", LiveOak.ID})));

    protected static Set<String> NOT_UPDATABLE = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            new String[] {"aliases", "chunkSize", "md5", "_id", LiveOak.ID, "createDate", "length", "contentType", LiveOak.SELF, "links", LiveOak.MEMBERS, "dir"})));


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

    public void updateFileInfo(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        // make sure to not update certain 'read-only' properties

        for (String name: state.getPropertyNames()) {
            if (!NOT_UPDATABLE.contains(name)) {
                Object value = state.getProperty(name);
                if (name.equals("parent") && value != null) {
                    fileInfo().dbObject().put(name, new ObjectId(value.toString()));
                } else {
                    fileInfo().dbObject().put(name, value);
                }
            }
        }
        getUserspace().getFilesCollection().save(fileInfo().dbObject());
        responder.resourceUpdated(this);
    }

    public void readFileInfo(PropertySink sink) {
        DBObject dbobj = fileInfo().dbObject();
        for (String key: dbobj.keySet()) {
            if (getFiltered().contains(key)) {
                continue;
            }
            Object val = dbobj.get(key);
            if (val instanceof Date) {
                val = ((Date) val).getTime();
            } else if (val instanceof ObjectId) {
                val = val.toString();
            }
            if ("uploadDate".equals(key)) {
                key = "createDate";
            }
            if (val != null) {
                sink.accept(key, val);
            }
        }
    }

    protected Set<String> getFiltered() {
        return FILTERED;
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
