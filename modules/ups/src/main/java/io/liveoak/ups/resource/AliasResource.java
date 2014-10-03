package io.liveoak.ups.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.ups.Alias;
import io.liveoak.ups.UPSSubscription;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class AliasResource implements SubscriptionResourceParent {

    private AliasesResource parent;
    private Alias alias;

    public AliasResource(AliasesResource parent, Alias alias) {
        this.parent = parent;
        this.alias = alias;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.alias.id();
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, String> result = new HashMap();
        result.put("subject", alias.subject());
        return result;
    }

    @Override
    public Collection<Resource> members(RequestContext ctx) throws Exception {
        return alias.getSubscriptions()
                .stream()
                .map(subs -> new SubscriptionResource(this, subs))
                .collect(Collectors.toList());
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

        // if the alias is specified, throw an error. The system handles aliases for this type of subscription
        if (state.getProperty("alias") != null && !state.getProperty("alias").equals(this.id())) {
            responder.invalidRequest("Alias not allowed to be customized when creating this type of subscription.");
            return;
        }

        List<String> aliases = new ArrayList<>();
        //NOTE: the alias is always the parent ID for this type of resources !
        aliases.add(alias.id());
        state.putProperty("alias", aliases);

        List<UPSSubscription> upsSubscriptions = alias.getSubscriptions();
        UPSSubscription subscription = UPSSubscription.create(state);
        if (subscription != null) {
            upsSubscriptions.add(subscription);
            alias.setSubscriptions(upsSubscriptions);
            parent.saveAlias(alias);
            responder.resourceCreated(new SubscriptionResource(this, subscription));
        } else {
            responder.invalidRequest("Cannot create a UPS Subscription without a resource-path specified");
        }
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        parent.deleteAlias(this.id());
        responder.resourceDeleted(this);
    }

    @Override
    public void updateSubscription(UPSSubscription upsSubscription) {
        alias.updateSubscription(upsSubscription);
        parent.saveAlias(alias);
    }

    @Override
    public void deleteSubscription(String id) {
        alias.removeSubscription(id);
        parent.saveAlias(alias);
    }
}
