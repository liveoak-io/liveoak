package org.projectodd.restafari.container.auth.impl;

import org.projectodd.restafari.container.auth.spi.ApplicationMetadata;
import org.projectodd.restafari.container.auth.spi.AuthPersister;
import org.projectodd.restafari.container.auth.spi.AuthorizationPolicyEntry;

import java.util.*;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InMemoryAuthPersister implements AuthPersister {

    // TODO: Thread-safety
    private Map<String, ApplicationMetadata> applicationMetadataMap = new HashMap<>();
    private Map<String, List<AuthorizationPolicyEntry>> authPolicies = new HashMap<>();

    @Override
    public void registerApplicationMetadata(ApplicationMetadata appMetadata) {
        applicationMetadataMap.put(appMetadata.getApplicationId(), appMetadata);
    }

    @Override
    public ApplicationMetadata getApplicationMetadata(String applicationKey) {
        return applicationMetadataMap.get(applicationKey);
    }

    @Override
    public List<AuthorizationPolicyEntry> getRegisteredPolicies(String applicationKey) {
        List<AuthorizationPolicyEntry> policies = authPolicies.get(applicationKey);
        return policies==null ? Collections.EMPTY_LIST : Collections.unmodifiableList(policies);
    }

    @Override
    public void registerPolicy(String applicationKey, AuthorizationPolicyEntry policy) {
        List<AuthorizationPolicyEntry> policies = authPolicies.get(applicationKey);
        if (policies == null) {
            policies = new ArrayList<>();
            authPolicies.put(applicationKey, policies);
        }
        policies.add(policy);
    }
}
