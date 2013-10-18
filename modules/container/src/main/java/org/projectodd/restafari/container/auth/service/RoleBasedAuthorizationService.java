package org.projectodd.restafari.container.auth.service;

import org.projectodd.restafari.container.auth.JsonWebToken;

import java.util.*;

/**
 * TODO: User managed access (only creator of resource XY can access this resource...)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RoleBasedAuthorizationService implements AuthorizationService {

    public static final String WILDCARD = "*";

    public static final RolePolicy ALLOW_ALL_ROLE_POLICY = new RolePolicy() {

        @Override
        public AuthorizationDecision isRealmRoleAllowed(String roleName) {
            return AuthorizationDecision.ALLOW;
        }

        @Override
        public AuthorizationDecision isApplicationRoleAllowed(String roleName) {
            return AuthorizationDecision.ALLOW;
        }

        @Override
        public AuthorizationDecision isRealmRolesAllowed(Set<String> roles) {
            return AuthorizationDecision.ALLOW;
        }

        @Override
        public AuthorizationDecision isApplicationRolesAllowed(Set<String> roles) {
            return AuthorizationDecision.ALLOW;
        }
    };

    private RecursiveHashMap permissions = new RecursiveHashMap(null);

    @Override
    public boolean isAuthorized(AuthorizationRequestContext authRequestContext) {
        List<String> segments = authRequestContext.getRequest().resourcePath().segments();
        int segmentsSize = segments.size();
        Deque<String> keys = new LinkedList<>();
        for (int i=0 ; i<3 ; i++) {
            if (i < segmentsSize) {
                keys.add(segments.get(i));
            } else {
                // Segments have less keys than 3 (request without collectionName or resourceId). Fill rest with * TODO: Maybe we should add different char than * here?
                keys.add(RoleBasedAuthorizationService.WILDCARD);
            }
        }

        // Add last key for action
        String action = authRequestContext.getRequest().requestType().name();
        keys.add(action);

        // Look for best RolePolicy
        RolePolicy rolePolicy = permissions.recursiveGet(keys);

        AuthorizationDecision authDecision = checkPermissions(rolePolicy, authRequestContext);
        return (authDecision != null && authDecision == AuthorizationDecision.ALLOW);
    }

    public void addRolePolicy(String type, String collectionName, String resourceId, String action, RolePolicy policy) {
        Deque<String> keys = new LinkedList<>();
        keys.add(type);
        keys.add(collectionName);
        keys.add(resourceId);
        keys.add(action);
        permissions.recursivePut(keys, policy);
    }

    protected AuthorizationDecision checkPermissions(RolePolicy rolePolicy, AuthorizationRequestContext authRequestContext) {
        JsonWebToken accessToken = authRequestContext.getAccessToken();

        // Use 'type' from resourcePath as appName for now
        String appName = authRequestContext.getRequest().resourcePath().head();

        Set<String> realmRoles = authRequestContext.isRequestAuthenticated() ? accessToken.getClaims().getRealmAccess().getRoles() : Collections.emptySet();
        Set<String> appRoles = getAppRoles(authRequestContext, appName);

        AuthorizationDecision realmRolesAuthDecision = rolePolicy.isRealmRolesAllowed(realmRoles);
        AuthorizationDecision appRolesDecision = rolePolicy.isApplicationRolesAllowed(appRoles);

        return mergeDecisions(realmRolesAuthDecision, appRolesDecision);
    }

    private Set<String> getAppRoles(AuthorizationRequestContext authRequestContext, String appName) {
        if (!authRequestContext.isRequestAuthenticated()) {
            return Collections.emptySet();
        }

        Map<String, JsonWebToken.Access> appAccess = authRequestContext.getAccessToken().getClaims().getResourceAccess();
        if (appAccess != null && appAccess.containsKey(appName)) {
            return appAccess.get(appName).getRoles();
        } else {
            return Collections.emptySet();
        }
    }

    protected AuthorizationDecision mergeDecisions(AuthorizationDecision authDec1, AuthorizationDecision authDec2) {
        if (authDec1 == AuthorizationDecision.DENY || authDec2 == AuthorizationDecision.DENY) {
            return AuthorizationDecision.DENY;
        } else if (authDec1 == AuthorizationDecision.ALLOW || authDec2 == AuthorizationDecision.ALLOW) {
            return AuthorizationDecision.ALLOW;
        } else {
            return AuthorizationDecision.NOT_SURE;
        }
    }
}
