package io.liveoak.container.subscriptions.resource;

import java.util.HashMap;
import java.util.Map;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.Subscription;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BasicSubscriptionResource implements SynchronousResource {

    public BasicSubscriptionResource(ApplicationSubscriptionsResource parent, Subscription subscription) {
        this.parent = parent;
        this.subscription = subscription;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.subscription.id();
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, String> result = new HashMap<>();
        result.put("resourcepath", subscription.resourcePath().toString());
        return result;
    }

    private ApplicationSubscriptionsResource parent;
    private Subscription subscription;
}
