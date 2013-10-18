package org.projectodd.restafari.mongo;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.resource.async.AbstractPaginatedCollectionResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;

/**
 * @author Bob McWhirter
 */
public class PaginatedMongoCollectionResource extends AbstractPaginatedCollectionResource<MongoCollectionResource> {

    public PaginatedMongoCollectionResource(MongoCollectionResource collection, Pagination pagination) {
        super(collection, pagination);
    }

    @Override
    public void writeMembers(ResourceSink sink) {
        DB db = collection().getDB();
        DBCollection collection = db.getCollection(collection().id());
        DBCursor cursor = collection.find().skip(pagination().getOffset()).limit(pagination().getOffset());

        cursor.forEach((e) -> {
            sink.accept(new MongoObjectResource(collection(), e));
        });

        sink.close();
    }
}
