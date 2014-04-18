/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri;


import io.liveoak.common.security.AuthzDecision;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * TODO: To be removed later (probably)
 *
 * Simple URI policy, which allows just wildcards (no custom patterns) in ResourcePath segments. Doesn't check request parameters
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SimpleURIPolicy {

    public static final String WILDCARD = "*";

    public static final RolesContainer ALLOW_ALL_ROLES_CONTAINER = new RolesContainer() {

        @Override
        public AuthzDecision isRoleAllowed(String roleName) {
            return AuthzDecision.ACCEPT;
        }

        @Override
        public AuthzDecision isUserAllowed(String username) {
            return AuthzDecision.ACCEPT;
        }

    };

    private RecursiveHashMap permissions = new RecursiveHashMap(null);

    public AuthzDecision isAuthorized(RequestContext req) {
        List<ResourcePath.Segment> segments = req.resourcePath().segments();
        int segmentsSize = segments.size();

        // TODO: Refactor this
        Deque<String> keys = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            if (i < segmentsSize) {
                keys.add(segments.get(i).name());
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

        AuthzDecision authDecision = checkPermissions(rolesContainer, req);
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

    protected AuthzDecision checkPermissions(RolesContainer rolesContainer, RequestContext reqCtx) {
        Set<String> roles = getRoles(reqCtx);

        AuthzDecision rolesAuthDecision = rolesContainer.isRolesAllowed(roles);

        return rolesAuthDecision;
    }

    private Set<String> getRoles(RequestContext reqCtx) {
        return reqCtx.securityContext().getRoles();
    }
}
