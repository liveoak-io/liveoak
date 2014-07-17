/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.util.LinkedList;
import java.util.List;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GridFSFileResource extends GridFSResource {

    public GridFSFileResource(RequestContext ctx, GridFSDirectoryResource parent, String id, GridFSDBObject info, GridFSResourcePath path) {
        super(ctx, parent, id, info, path);
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
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        try {

            readFileInfo(ctx, sink);

            String blobPath = getBlobUri();
            String selfPath = getSelfUri();
            String parentPath = getParentUri();

            List links = new LinkedList();

            links.add(new MapResource()
                    .put("rel", "self")
                    .put("href", selfPath));

            links.add(new MapResource()
                    .put("rel", "parent")
                    .put("href", parentPath));

            links.add(new MapResource()
                    .put("rel", "blob")
                    .put("href", blobPath));

            sink.accept("links", links);

        } finally {
            sink.close();
        }
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        updateFileInfo(ctx, state, responder);
    }

    @Override
    public String toString() {
        return "[GridFSFileResource: id=" + this.id() + ", path=" + path() + "]";
    }
}
