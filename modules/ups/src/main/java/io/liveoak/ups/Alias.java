package io.liveoak.ups;

import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class Alias {

    DBObject dbObject;

    private Alias (DBObject dbObject) {
       this.dbObject = dbObject;
    }

    public static Alias create(DBObject dbObject) {
        return new Alias(dbObject);
    }

    public String id() {
        return (String)dbObject.get("_id");
    }

    public String subject() {
        return (String)dbObject.get("subject");
    }

    public List<UPSSubscription> getSubscriptions() {
        List<UPSSubscription> upsSubscriptions = new ArrayList<>();

        Object subscriptions = dbObject.get("subscriptions");
        if (subscriptions != null && subscriptions instanceof List) {

            for (Object subscription: (List)subscriptions) {
                if (subscription instanceof DBObject) {
                    UPSSubscription upsSubscription = UPSSubscription.create( ( DBObject ) subscription );
                    if (upsSubscription != null) {
                        upsSubscriptions.add(upsSubscription);
                    }
                }
            }
        }

        return upsSubscriptions;
    }

    public void setSubscriptions(List<UPSSubscription> upsSubscriptions) {
        List<DBObject> dbList = new ArrayList<>();
        for ( UPSSubscription upsSubscription: upsSubscriptions) {
            dbList.add( upsSubscription.dbObject());
        }
        dbObject.put("subscriptions", dbList);
    }

    public void updateSubscription( UPSSubscription upsSubscription) {
         List<UPSSubscription> upsSubscriptions = getSubscriptions();
         for ( UPSSubscription savedSubscription: upsSubscriptions) {
            if (upsSubscription.id().equals(upsSubscription.id())) {
                // remove the old subscription
                upsSubscriptions.remove(savedSubscription);
                // add the new one
                upsSubscriptions.add(upsSubscription);
                break;
            }
         }
        // save the change
        setSubscriptions(upsSubscriptions);
    }

    public void removeSubscription(String id) {
        List<UPSSubscription> upsSubscriptions = getSubscriptions();
        for ( UPSSubscription upsSubscription: upsSubscriptions) {
            if (upsSubscription.id().equals(id)) {
                // remove the subscription
                upsSubscriptions.remove(upsSubscription);
                break;
            }
        }
        // save the change
        setSubscriptions(upsSubscriptions);
    }

    public DBObject dbObject() {
        return this.dbObject;
    }

}
