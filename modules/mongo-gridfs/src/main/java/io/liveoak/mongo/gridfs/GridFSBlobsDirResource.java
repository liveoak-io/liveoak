/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Responder;
import org.bson.types.ObjectId;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GridFSBlobsDirResource extends GridFSDirectoryResource {

    public GridFSBlobsDirResource(RequestContext ctx, GridFSDirectoryResource parent, String id, GridFSResourcePath path) {
        super(ctx, parent, id, path);
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        DBCollection col = getUserspace().getFilesCollection();
        DBObject result = col.findOne(new ObjectId(id));
        if (result == null) {
            responder.noSuchResource(id);
            return;
        }

        responder.resourceRead(new GridFSBlobResource(ctx, this, id, new GridFSDBObject(result), path().append(id)));
    }

    @Override
    public String toString() {
        return "[GridFSBlobsDirResource: id=" + this.id() + ", path=" + path() + "]";
    }
}
