/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Supplier;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.BinaryContentSink;
import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.LazyResourceState;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import org.bson.types.ObjectId;
import org.vertx.java.core.file.AsyncFile;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GridFSBlobResource extends GridFSResource implements BlockingResource, BinaryResource {

    public GridFSBlobResource(RequestContext ctx, GridFSDirectoryResource parent, String id, GridFSDBObject fileInfo, GridFSResourcePath path) {
        super(ctx, parent, id, fileInfo, path);
    }

    @Override
    public MediaType mediaType() {
        return new MediaType(fileInfo().getString("contentType"));
    }

    @Override
    public long contentLength() {
        return fileInfo().getLong("length");
    }

    @Override
    public void readContent(RequestContext ctx, BinaryContentSink sink) throws Exception {

        GridFS gridfs = getUserspace().getGridFS();

        // TODO - maybe use netty directly here?
        // TODO - BinaryContentSink should flush to socket (pipeline channel), not queue to memory

        // get tmp file
        File tmpFile = getTempFile();

        // sync copy from db to tmp file first - no async API
        GridFSDBFile file = gridfs.findOne(new BasicDBObject("_id", fileInfo().getId()));
        file.writeTo(tmpFile);

        // async copy from local tmp file to sink
        getRoot().vertx().fileSystem().open(tmpFile.getPath(), (result) -> {
            if (result.succeeded()) {
                AsyncFile asyncFile = result.result();
                asyncFile.dataHandler((buffer) -> {
                    sink.accept(buffer.getByteBuf());
                });
                asyncFile.endHandler((end) -> {
                    sink.close();
                    tmpFile.delete();
                });
            } else {
                sink.close();
                tmpFile.delete();
            }
        });
    }

    private File getTempFile() {
        // use configured temp.dir for temp file location
        return new File(getRoot().tempDir(), UUID.randomUUID().toString());
    }

    @Override
    public boolean willProcessUpdate(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        // no reason to reject reading the body
        return true;
    }

    public void updateContent(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        System.err.println( "UPDATE CONTENT" );
        if (state instanceof LazyResourceState == false) {
            responder.internalError("Expected state instanceof LazyResourceState, not " + state.getClass());
        }

        GridFSDBObject blob = fileInfo();
        boolean isNew = blob.getId() == null;

        LazyResourceState request = (LazyResourceState) state;
        if (request.hasBigContent()) {
            File tmpFile = request.contentAsFile();
            if (tmpFile != null) {
                try {
                    GridFSFilesPathItemResource response = pushToDB(ctx, request.getContentType(), blob, () -> { return tmpFile; });
                    if (isNew) {
                        responder.resourceCreated(response);
                    } else {
                        responder.resourceUpdated(response);
                    }
                } catch (Exception e) {
                    responder.internalError(e);
                }
            } else {
                responder.internalError("No cache file for request: " + request);
            }
        } else {
            InputStream stream = request.contentAsStream();
            if (stream != null) {
                try {
                    GridFSFilesPathItemResource response = pushToDB(ctx, request.getContentType(), fileInfo(), () -> { return stream; });
                    if (isNew) {
                        responder.resourceCreated(response);
                    } else {
                        responder.resourceUpdated(response);
                    }
                } catch (Exception e) {
                    responder.internalError(e);
                }
            } else {
                responder.internalError("No Stream for request: " + request);
            }
        }
    }

    public void delete(RequestContext ctx, Responder responder) throws Exception {
        System.err.println( "DELETE CONTENT" );

        GridFSDBObject info = fileInfo();
        if (info.getId() == null) {
            responder.noSuchResource(id());
            return;
        }
        GridFS gridfs = getUserspace().getGridFS();
        gridfs.remove(info.getId());
        info.dbObject().put("length", 0L);
        info.dbObject().put("contentType", null);
        responder.resourceDeleted(this);
    }

    private GridFSFilesPathItemResource pushToDB(RequestContext ctx, MediaType contentType, GridFSDBObject fileInfo, Supplier contentProvider) throws IOException {
        ObjectId currentId = fileInfo.getId();
        boolean fileExists = currentId != null;

        // update the targeted userspace - hopefully current user has rights to do that
        GridFS gridfs = getUserspace().getGridFS();

        Object content = contentProvider.get();
        GridFSInputFile blob;
        if (fileExists) {
            // here is a time gap when file doesn't exist for a while when being updated.
            // making the switch instantaneous would require another layer of indirection
            // - not using file_id as canonical id, but some other id, mapped to a file.
            // there would still remain a moment between mapping from old file to new file
            // involving two separate file items and a moment in time when during a switch
            // no file would match a filename, nor file id.
            gridfs.remove(currentId);
        }
        if (content instanceof File) {
            blob = gridfs.createFile((File) content);
        } else if (content instanceof InputStream) {
            blob = gridfs.createFile((InputStream) content);
        } else if (content instanceof ByteBuf) {
            blob = gridfs.createFile(((ByteBuf) content).array());
        } else {
            throw new IllegalArgumentException("Unsupported value supplied: " + content.getClass());
        }

        // meta data
        if (fileExists) {
            blob.setId(currentId);
        }
        blob.setFilename(fileInfo().getString("filename"));
        blob.setContentType(contentType != null ? contentType.toString() : "application/octet-stream");
        blob.put("parent", fileInfo().getParentId());
        blob.save();

        String oid = blob.getId().toString();

        return new GridFSFilesPathItemResource(ctx,
                getFilesRoot(), oid, new GridFSDBObject(blob), GridFSResourcePath.fromContext(ctx));
    }

    @Override
    public String toString() {
        return "[GridFSBlobResource: id=" + this.id() + ", path=" + path() + "]";
    }
}
