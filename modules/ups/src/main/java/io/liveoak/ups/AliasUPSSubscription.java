package io.liveoak.ups;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class AliasUPSSubscription extends BaseUPSSubscription {

    public AliasUPSSubscription(DBCollection collection, UPS ups) {
        super(collection, ups);
    }

    @Override
    protected List<UPSSubscription> getSubscriptions(String uri) {
        List<UPSSubscription> subscriptions = new ArrayList<>();

        List<String> listenerPaths = generatePaths(uri);

        DBObject match = new BasicDBObject("$match", new BasicDBObject("subscriptions.resource-path", new BasicDBObject("$in", listenerPaths)));
        DBObject unwind = new BasicDBObject("$unwind", "$subscriptions");

        // first perform a match to get the object which contains a subscription we want [uses the index]
        // then unwind to get the individual subscriptions
        // then match again to get only the subscriptions we want.
        // NOTE: the first step is not redundant, we need to first narrow down using an index (for performance) before unwinding and ultimately getting only the results we want.
        AggregationOutput aggregate = collection.aggregate(match, unwind, match);
        for (DBObject dbObject : aggregate.results()) {
            UPSSubscription subscription = UPSSubscription.create((DBObject) dbObject.get("subscriptions"));
            if (subscription != null) {
                subscriptions.add(subscription);
            }
        }

        return subscriptions;
    }
}
