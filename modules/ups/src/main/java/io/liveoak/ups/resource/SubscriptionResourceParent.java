package io.liveoak.ups.resource;

import io.liveoak.spi.resource.async.Resource;
import io.liveoak.ups.UPSSubscription;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface SubscriptionResourceParent extends Resource {

   void updateSubscription(UPSSubscription upsSubscription);

   void deleteSubscription(String id);

}
