/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
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
    public Resource member(RequestContext ctx, String id) {
        // delegate to the proper resource based on url
        if (".files".equals(id)) {
            return new GridFSFilesDirResource(ctx, this, id, path().append(id));
        } else if (".blobs".equals(id)) {
            return new GridFSBlobsDirResource(ctx, this, id, path().append(id));
        } else {
            return super.member(ctx, id);
        }
    }

    @Override
    public Collection<Resource> members(RequestContext ctx) throws Exception {

        DBCollection col = getFilesCollection();
        String rootId = getRootDirId(col);
        if (rootId == null) {
            // no root yet => no children
            return Collections.emptyList();
        }

        LinkedList members = new LinkedList<>();
        // find children of root
        DBCursor result =  col.find(new BasicDBObject("parent", new ObjectId(rootId)));
        while (result.hasNext()) {
            DBObject child = result.next();
            members.add(wrapDBObject(path(), new GridFSDBObject(child)));
        }
        return members;
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
