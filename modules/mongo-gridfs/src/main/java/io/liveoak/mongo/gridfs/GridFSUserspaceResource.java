/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import org.bson.types.ObjectId;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GridFSUserspaceResource extends GridFSDirectoryResource {

    private String rootDirId;

    public GridFSUserspaceResource(RequestContext ctx, GridFSDirectoryResource parent, String id, GridFSResourcePath remainingPath) {
        super(ctx, parent, id, remainingPath);
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        // delegate to the proper resource based on url
        if (".files".equals(id)) {
            responder.resourceRead(new GridFSFilesDirResource(ctx, this, id, path().append(id)));
        } else if (".blobs".equals(id)) {
            responder.resourceRead(new GridFSBlobsDirResource(ctx, this, id, path().append(id)));
        } else {
            super.readMember(ctx, id, responder);
        }
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        DBCollection col = getFilesCollection();
        String rootId = getRootDirId(col);
        if (rootId == null) {
            // no root yet => no children
            sink.close();
            return;
        }

        // find children of root
        DBCursor result =  col.find(new BasicDBObject("parent", new ObjectId(rootId)));
        while (result.hasNext()) {
            DBObject child = result.next();
            sink.accept(wrapDBObject(path(), new GridFSDBObject(child)));
        }
        sink.close();
    }


    protected String getRootDirId(DBCollection col) {
        if (this.rootDirId == null) {
            DBObject rootObj = col.findOne(new BasicDBObject("parent", null).append("filename", ""));
            if (rootObj != null) {
                rootDirId = new GridFSDBObject(rootObj).getId().toString();
            }
        }
        return rootDirId;
    }

    public DBCollection getFilesCollection() {
        return getRoot().getDB().getCollection(id() + ".files");
    }


    public GridFS getGridFS() {
        return new GridFS(getRoot().getDB(), id());
    }

    @Override
    public String toString() {
        return "[GridFSUserspaceResource: id=" + this.id() + ", path=" + path() + "]";
    }
}
