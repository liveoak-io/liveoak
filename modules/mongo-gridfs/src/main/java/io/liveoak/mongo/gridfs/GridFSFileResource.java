/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.liveoak.mongo.gridfs.util.MapPropertySink;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.spi.resource.MapResource;

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
    public Map<String, ?> properties(RequestContext ctx) throws Exception {

        Map<String, Object> result = new HashMap<>();
        readFileInfo(new MapPropertySink(result));

        String blobPath = getBlobUri();
        String selfPath = getSelfUri();
        String parentPath = getParentUri();

        List links = new LinkedList();

        links.add(new MapResource()
                .put("rel", "self")
                .put(LiveOak.HREF, selfPath));

        links.add(new MapResource()
                .put("rel", "parent")
                .put(LiveOak.HREF, parentPath));

        links.add(new MapResource()
                .put("rel", "blob")
                .put(LiveOak.HREF, blobPath));

        result.put("links", links);
        return result;
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
