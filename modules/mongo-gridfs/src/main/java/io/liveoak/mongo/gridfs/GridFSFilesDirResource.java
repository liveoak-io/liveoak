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
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import org.bson.types.ObjectId;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GridFSFilesDirResource extends GridFSDirectoryResource {

    public GridFSFilesDirResource(RequestContext ctx, GridFSUserspaceResource parent, String id, GridFSResourcePath remainingPath) {
        super(ctx, parent, id, remainingPath);
    }

    @Override
    public GridFSUserspaceResource parent() {
        return (GridFSUserspaceResource) super.parent();
    }

    @Override
    protected String getSelfUri() {
        return uri().toString();
    }

    @Override
    protected String getParentUri() {
        return parent().uri().toString();
    }

    @Override
    protected String getBlobUri() {
        return null;
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {

        // processing the last uri segment
        DBCollection col = parent().getFilesCollection();
        DBObject result = col.findOne(new BasicDBObject("_id", new ObjectId(id)));
        if (result == null) {
            responder.noSuchResource(id);
            return;
        }

        GridFSDBObject info = new GridFSDBObject(result);
        if (info.isTrue("dir")) {
            // if directory
            responder.resourceRead(newChildDir(new GridFSResourcePath(id), info));
        } else {
            // if file
            responder.resourceRead(newChildItem(info));
        }
    }

    @Override
    protected GridFSDirectoryResource newChildDir(GridFSResourcePath path, GridFSDBObject item) {
        return new GridFSFilesDirItemResource(requestContext(), (GridFSFilesDirResource) getChildParent(),
                item.getId().toString(), path.append(item.getId().toString()), item);
    }

    @Override
    protected GridFSResource newChildItem(GridFSDBObject item) {
        return new GridFSFilesItemResource(requestContext(), getChildParent(),
                item.getId().toString(), item, path().append(item.getId().toString()));
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {

        // if children requested query children info
        DBCollection col = getUserspace().getFilesCollection();
        DBCursor result = col.find().skip(ctx.pagination().offset()).limit(ctx.pagination().limit());

        while (result.hasNext()) {
            DBObject child = result.next();
            System.err.println( "** CHILD: " + child );
            sink.accept(wrapDBObject(path(), new GridFSDBObject(child)));
        }

        sink.close();
    }

    @Override
    public String toString() {
        return "[GridFSFilesDirResource: id=" + this.id() + ", path=" + path() + "]";
    }
}
