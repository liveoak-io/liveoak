/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.io.IOException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GridFSReadTest extends BaseGridFSTest {

    @Test
    public void testCreateGridFS() throws IOException {
        String userId = "some-user-id";

        // each user has their own space named after their userId

        // gridfs can be accessed the classic way ... using .files directly
        // first drop gridfs storage for user
        DBCollection files = db.getCollection(userId + ".files");
        files.drop();

        DBCollection chunks = db.getCollection(userId + ".chunks");
        chunks.drop();

        // create root folder
        files = db.getCollection(userId + ".files");
        BasicDBObject dir = new BasicDBObject("filename", "").append("dir", true);
        files.insert(dir);

        // create subdir
        dir = new BasicDBObject("filename", "subdir").append("parent", dir.get("_id")).append("dir", true);
        files.insert(dir);

        // create/write file in that subdir
        GridFS fs = new GridFS(db, userId);
        GridFSInputFile blob = fs.createFile(new SampleInputStream(1024 * 1024));

        // meta data
        blob.setFilename("some-file.txt");
        blob.setContentType("text/plain");
        blob.put("parent", dir.get("_id"));
        //BasicDBObject meta = new BasicDBObject();
        //blob.setMetaData(meta);
        blob.save();

        // now let's start at the root and navigate down to the file we created
        // get root
        DBObject found = files.findOne(new BasicDBObject("filename", "").append("parent", null).append("dir", true));

        // list root children
        DBCursor rs = files.find(new BasicDBObject("parent", found.get("_id")));
        assertThat(rs.size()).isEqualTo(1);
        while(rs.hasNext()) {
            found = rs.next();
        }

        assertThat(found.get("dir")).isEqualTo(true);
        assertThat(found.get("filename")).isEqualTo("subdir");
        // list subdir children
        BasicDBObject dirId = new BasicDBObject("parent", found.get("_id"));
        rs = files.find(dirId);
        assertThat(rs.size()).isEqualTo(1);
        while(rs.hasNext()) {
            found = rs.next();
        }
        assertThat(found.get("dir")).isNull();

        // read file
        GridFSDBFile file = fs.findOne(new BasicDBObject("_id", found.get("_id")));
        CountOutputStream out = new CountOutputStream();
        file.writeTo(out);

        assertThat(out.getCount()).isEqualTo(1024 * 1024);

        // delete file
        fs.remove(new BasicDBObject("_id", found.get("_id")));

        // make sure that listing parent does not return the file any more
        rs = files.find(dirId);
        assertThat(rs.size()).isEqualTo(0);
    }
}
