/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri.simple;


import io.liveoak.security.impl.AuthServicesHolder;
import io.liveoak.security.policy.uri.RolesContainer;
import io.liveoak.security.spi.AuthorizationDecision;
import io.liveoak.security.spi.AuthorizationPolicy;
import io.liveoak.security.spi.AuthorizationRequestContext;
import io.liveoak.spi.RequestContext;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple URI policy, which allows just wildcards (no custom patterns) in ResourcePath segments. Doesn't check request parameters
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SimpleURIPolicy implements AuthorizationPolicy {

    public static final String WILDCARD = "*";

    public static final RolesContainer ALLOW_ALL_ROLES_CONTAINER = new RolesContainer() {

        @Override
        public AuthorizationDecision isRealmRoleAllowed(String roleName) {
            return AuthorizationDecision.ACCEPT;
        }

        @Override
        public AuthorizationDecision isApplicationRoleAllowed(String roleName) {
            return AuthorizationDecision.ACCEPT;
        }

        @Override
        public AuthorizationDecision isRealmRolesAllowed(Collection<String> roles) {
            return AuthorizationDecision.ACCEPT;
        }

        @Override
        public AuthorizationDecision isApplicationRolesAllowed(Collection<String> roles) {
            return AuthorizationDecision.ACCEPT;
        }
    };

    private RecursiveHashMap permissions = new RecursiveHashMap(null);


    @Override
    public void init() {
        // Empty by default
    }

    @Override
    public AuthorizationDecision isAuthorized(AuthorizationRequestContext authRequestContext) {
        RequestContext req = authRequestContext.getRequestContext();
        List<String> segments = req.resourcePath().segments();
        int segmentsSize = segments.size();

        // TODO: Refactor this
        Deque<String> keys = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            if (i < segmentsSize) {
                keys.add(segments.get(i));
            } else {
                // Segments have less keys than 3 (request without collectionName or resourceId). Fill rest with * TODO: Maybe we should add different char than * here?
                keys.add(SimpleURIPolicy.WILDCARD);
            }
        }

        // Add last key for action
        String action = req.requestType().name();
        keys.add(action);

        // Look for best RolesContainer
        RolesContainer rolesContainer = permissions.recursiveGet(keys);

        // Find applicationName from persister, so we can obtain applicationRoles for correct application from token
        String appId = AuthServicesHolder.getInstance().getApplicationIdResolver().resolveAppId(req);
        String appName = AuthServicesHolder.getInstance().getAuthPersister().getApplicationMetadata(appId).getApplicationName();

        AuthorizationDecision authDecision = checkPermissions(rolesContainer, authRequestContext, appName);
        return authDecision;
    }

    public void addRolePolicy(String type, String collectionName, String resourceId, String action, RolesContainer policy) {
        Deque<String> keys = new LinkedList<>();
        keys.add(type);
        keys.add(collectionName);
        keys.add(resourceId);
        keys.add(action);
        permissions.recursivePut(keys, policy);
    }

    protected AuthorizationDecision checkPermissions(RolesContainer rolesContainer, AuthorizationRequestContext authRequestContext, String applicationName) {

        Set<String> realmRoles = getRealmRoles(authRequestContext);
        Set<String> appRoles = getAppRoles(authRequestContext, applicationName);

        AuthorizationDecision realmRolesAuthDecision = rolesContainer.isRealmRolesAllowed(realmRoles);
        AuthorizationDecision appRolesDecision = rolesContainer.isApplicationRolesAllowed(appRoles);

        return realmRolesAuthDecision.mergeDecision(appRolesDecision);
    }

    private Set<String> getRealmRoles(AuthorizationRequestContext authRequestContext) {
        return authRequestContext.getAuthToken().getRealmRoles();
    }

    private Set<String> getAppRoles(AuthorizationRequestContext authRequestContext, String appName) {
        if (!authRequestContext.isRequestAuthenticated()) {
            return Collections.emptySet();
        }

        Map<String, Set<String>> appAccess = authRequestContext.getAuthToken().getApplicationRolesMap();
        if (appAccess.containsKey(appName)) {
            return appAccess.get(appName);
        } else {
            return Collections.EMPTY_SET;
        }
    }
}
