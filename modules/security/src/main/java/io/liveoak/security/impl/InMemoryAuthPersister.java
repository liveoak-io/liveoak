/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.impl;

import io.liveoak.security.spi.ApplicationMetadata;
import io.liveoak.security.spi.AuthPersister;
import io.liveoak.security.spi.AuthorizationPolicyEntry;

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
