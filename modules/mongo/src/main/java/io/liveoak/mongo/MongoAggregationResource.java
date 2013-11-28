/*
* Copyright 2013 Red Hat, Inc. and/or its affiliates.
*
* Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
*/
package io.liveoak.mongo;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MongoAggregationResource extends MongoAggregationItem {

    MongoAggregationResource(MongoCollectionResource parent) {
        super(parent, null);
    }

    @Override
    public MongoCollectionResource parent() {
        return (MongoCollectionResource) super.parent();
    }

    private BasicDBList aggregate(RequestContext ctx) {
        BasicDBList queryObject = new BasicDBList();
        if (ctx.getResourceParams() != null && ctx.getResourceParams().contains("q")) {
            String queryString = ctx.getResourceParams().value("q");
            DBObject paramObject = (DBObject) JSON.parse(queryString);

            if (paramObject instanceof BasicDBList) {
                queryObject = (BasicDBList) paramObject;
            } else {
                queryObject.add(paramObject);
            }
        }

        DBCollection dbCollection = parent().dbCollection;

        try {
            BasicDBList result = new BasicDBList();
            AggregationOutput output = dbCollection.aggregate(
                    (DBObject) queryObject.remove(0),
                    queryObject.toArray(new DBObject[queryObject.size()]));
            for (DBObject dbObject : output.results()) {
                result.add(dbObject);
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Aggregation query failed: ", e);
        }
    }


    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("result", getResourceCollection(aggregate(ctx)));
        sink.close();
    }

    @Override
    public String id() {
        return "_aggregate";
    }

    public String toString() {
        return "[MongoAggregationResource: for id:" + parent().id() + "]";
    }

}
