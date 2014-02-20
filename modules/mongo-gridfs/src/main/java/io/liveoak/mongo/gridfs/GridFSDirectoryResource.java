/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.bson.types.ObjectId;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GridFSDirectoryResource extends GridFSResource {

    protected static Set<String> FILTERED = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            new String[]{"aliases", "chunkSize", "_id", "id", "length", "contentType"})));

    public GridFSDirectoryResource(RequestContext ctx, GridFSDirectoryResource parent, String id, GridFSResourcePath path) {
        this(ctx, parent, id, path, null);
    }

    public GridFSDirectoryResource(RequestContext ctx, GridFSDirectoryResource parent, String id, GridFSResourcePath path, GridFSDBObject fileInfo) {
        super(ctx, parent, id, fileInfo, path);
    }

    @Override
    protected String getSelfUri() {
        return path().toString();
    }

    @Override
    protected String getParentUri() {
        return path().parent().toString();
    }

    @Override
    protected String getBlobUri() {
        return null;
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {

        GridFSResourcePath childPath = path().append(id);

        System.err.println( "read: " + childPath );

        if (childPath.equals(ctx.resourcePath())) {

            System.err.println( "create target file: " + childPath );
            // there are no more intermediary segments - this is the last parent,
            // here we lookup / generate the target GridFS file

            LinkedList<ResourcePath.Segment> segments = new LinkedList(ctx.resourcePath().segments());
            // skip app
            segments.removeFirst();
            // skip gridfsroot
            segments.removeFirst();
            // init meta
            boolean meta = segments.getLast().matrixParameters().containsKey("meta");

            DBCollection col = getUserspace().getFilesCollection();

            DBObject result = null;
            GridFSDBObject last = null;
            int count = 0;
            for (ResourcePath.Segment segment: segments) {
                count++;

                // first segment represents root - root file has empty string for a name, and null parent
                String name = count == 1 ? "" : segment.name();
                ObjectId parentId = count == 1 ? null : last.getId();

                result = col.findOne(new BasicDBObject("filename", name).append("parent", parentId));
                if (result == null) {
                    if (ctx.requestType() == RequestType.UPDATE) {
                        // create fileInfo for current segment
                        BasicDBObject fileInfo = new BasicDBObject("filename", name).append("owner", ctx.securityContext().getSubject());
                        if (last != null) {
                            fileInfo.append("parent", last.getId());
                        }

                        // insert for directories but not for files
                        // files get inserted via GridFS API in GridFSBlobResource
                        if (count < segments.size()) {
                            fileInfo.append("dir", true);
                            // autocreate missing parent directories when putting a blob
                            col.insert(fileInfo);
                        }
                        result = fileInfo;
                    } else {
                        responder.noSuchResource(id);
                        return;
                    }
                }
                last = new GridFSDBObject(result);
            }

            // finally we got to the fileInfo representing the target resource
            if (last.isTrue("dir")) {
                // if target resource represents a directory
                responder.resourceRead(newChildDir(path(), last));
            } else {
                // if file
                if (meta) {
                    // if last segment has matrix parameter 'meta' return meta info instead of blob content
                    responder.resourceRead(newChildItem(last));
                } else {
                    // if no ;meta, then return a blob
                    responder.resourceRead(new GridFSBlobResource(ctx, this, id, last, childPath));
                }
            }
        } else if (childPath.segments().size() == ctx.resourcePath().segments().size()) {
            responder.noSuchResource(id);
        } else {
            // pass-through segment
            responder.resourceRead(new GridFSDirectoryResource(ctx, this, id, childPath));
        }

    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        try {
            if (fileInfo() != null) {
                readFileInfo(ctx, sink);
            }
            //sink.accept("owner", fileInfo.get("owner"));
            sink.accept("dir", true);

            String selfPath = getSelfUri();
            String parentPath = getParentUri();

            List links = new LinkedList();
            links.add(new MapResource()
                    .put("rel", "self")
                    .put("href", selfPath));

            if (parentPath != null) {
                links.add(new MapResource()
                        .put("rel", "parent")
                        .put("href", parentPath));
            }
            sink.accept("links", links);
        } finally {
            sink.close();
        }
    }

    protected Set<String> getFiltered() {
        return FILTERED;
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        updateFileInfo(ctx, state, responder);
    }

    protected GridFSDirectoryResource getChildParent() {
        return getFilesRoot();
    }

    protected GridFSDirectoryResource newChildDir(GridFSResourcePath path, GridFSDBObject item) {
        return new GridFSDirectoryResource(requestContext(), getChildParent(),
                item.getId().toString(), path.append(item.getString("filename")), item);
    }

    protected GridFSResource newChildItem(GridFSDBObject item) {
        return new GridFSFileResource(requestContext(), getChildParent(),
                item.getId().toString(), item, path().append(item.getString("filename")));
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {

        // list child fileInfos
        if (fileInfo() == null) {
            throw new IllegalStateException("Internal error: fileInfo == null");
        }

        DBCollection col = getUserspace().getFilesCollection();
        DBCursor result = col.find(new BasicDBObject("parent", fileInfo().getId()));

        while (result.hasNext()) {
            DBObject child = result.next();
            sink.accept(wrapDBObject(path(), new GridFSDBObject(child)));
        }

        sink.close();
    }

    protected GridFSResource wrapDBObject(GridFSResourcePath parentPath, GridFSDBObject item) {
        if (item.isTrue("dir")) {
            return newChildDir(parentPath, item);
        } else {
            return newChildItem(item);
        }
    }

    @Override
    public String toString() {
        return "[GridFSDirectoryResource: id=" + this.id() + ", path=" + path() + "]";
    }
}
