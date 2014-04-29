package io.liveoak.ups;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.SecurityContext;
import io.liveoak.spi.container.Subscription;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseUPSSubscription implements Subscription {

    private static final String ID = "ups-resources";
    private static final ResourcePath RESOURCE_PATH = new ResourcePath("/*");

    DBCollection collection;
    UPS ups;

    public BaseUPSSubscription(DBCollection collection, UPS ups) {
        this.collection = collection;
        this.ups = ups;
    }

    @Override
    public String id() {
        return this.ID;
    }

    @Override
    public ResourcePath resourcePath() {
        // the resources path matches everything "/*"
        // This way we subscribe to everything and do the check in the datastore.
        // The default ResourceManager doesn't support persistent data.
        return this.RESOURCE_PATH;
    }

    // Due to the nature of the endpoint and the levels of separation. We cannot determine if an
    // end user currently has permission or not.
    @Override
    public boolean isSecure() {
        return false;
    }

    // Due to the nature of the endpoint and its security, we use an ANONYMOUS
    // SecurityContext here
    @Override
    public SecurityContext securityContext() {
        return SecurityContext.ANONYMOUS;
    }

    // Not sure why we have to have this method here...
    @Override
    public void sendAuthzError( ResourceState errorState, Resource resource, int status ) throws Exception {
        throw new IllegalStateException("Authorization error not expected for SubscriptionResource");
    }

    @Override
    public void resourceCreated( ResourceResponse resourceResponse ) throws Exception {
        sendNotification( resourceResponse, UPS.EventType.CREATED );
    }

    @Override
    public void resourceUpdated( ResourceResponse resourceResponse ) throws Exception {
        sendNotification( resourceResponse, UPS.EventType.UPDATED );
    }

    @Override
    public void resourceDeleted( ResourceResponse resourceResponse ) throws Exception {
        sendNotification( resourceResponse, UPS.EventType.DELETED );
    }


    private void sendNotification( ResourceResponse resourceResponse, UPS.EventType eventType ) {
        URI resourceURI =   resourceResponse.resource().uri();
        List<UPSSubscription> subscriptions = getSubscriptions(resourceURI.toString());

        for (UPSSubscription subscription: subscriptions) {
            ups.send(resourceURI, eventType, subscription);
        }
    }

    protected List<UPSSubscription> getSubscriptions(String uri) {
        List<UPSSubscription> subscriptions = new ArrayList<>();

        List<String> listenerPaths = generatePaths(uri);

        DBObject query = new BasicDBObject();
        query.put("resource-path", new BasicDBObject( "$in", listenerPaths ) );
        query.put("enabled", true);

        DBCursor cursor = collection.find(query);
        while ( cursor.hasNext() ) {
            DBObject dbObject = cursor.next();
            UPSSubscription subscription = UPSSubscription.create(dbObject);
            if (subscription != null) {
                subscriptions.add(subscription);
            }
        }

        return subscriptions;
    }

    // generates a list of paths which could be used to listen for this particular resources.
    // the URI is /foo/bar/baz then the possible listeners could be registered on: /foo/bar/baz, /foo/bar/*, /foo/*, /*
    protected List<String> generatePaths(String uri) {
        List<String> paths = new ArrayList<>();

        ResourcePath resourcePath = new ResourcePath(uri);
        paths.add(resourcePath.toString());
        while (!resourcePath.segments().isEmpty()) {
            resourcePath = resourcePath.parent();
            paths.add(resourcePath.toString() + "/*");
        }

        return paths;
    }
}
