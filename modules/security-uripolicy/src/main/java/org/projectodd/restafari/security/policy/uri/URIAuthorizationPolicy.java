package org.projectodd.restafari.security.policy.uri;


import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.projectodd.restafari.security.impl.AuthServicesHolder;
import org.projectodd.restafari.security.spi.AuthorizationDecision;
import org.projectodd.restafari.security.spi.AuthorizationPolicy;
import org.projectodd.restafari.security.spi.AuthorizationRequestContext;
import org.projectodd.restafari.security.impl.JsonWebToken;
import org.projectodd.restafari.spi.RequestContext;

/**
 * TODO: User managed access (only creator of resource XY can access this resource...)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIAuthorizationPolicy implements AuthorizationPolicy {

    public static final String WILDCARD = "*";

    public static final RolePolicy ALLOW_ALL_ROLE_POLICY = new RolePolicy() {

        @Override
        public AuthorizationDecision isRealmRoleAllowed(String roleName) {
            return AuthorizationDecision.ACCEPT;
        }

        @Override
        public AuthorizationDecision isApplicationRoleAllowed(String roleName) {
            return AuthorizationDecision.ACCEPT;
        }

        @Override
        public AuthorizationDecision isRealmRolesAllowed(Set<String> roles) {
            return AuthorizationDecision.ACCEPT;
        }

        @Override
        public AuthorizationDecision isApplicationRolesAllowed(Set<String> roles) {
            return AuthorizationDecision.ACCEPT;
        }
    };

    private RecursiveHashMap permissions = new RecursiveHashMap(null);

    @Override
    public AuthorizationDecision isAuthorized(AuthorizationRequestContext authRequestContext) {
        RequestContext req = authRequestContext.getRequestContext();
        List<String> segments = req.getResourcePath().segments();
        int segmentsSize = segments.size();

        // TODO: Refactor this
        Deque<String> keys = new LinkedList<>();
        for (int i=0 ; i<3 ; i++) {
            if (i < segmentsSize) {
                keys.add(segments.get(i));
            } else {
                // Segments have less keys than 3 (request without collectionName or resourceId). Fill rest with * TODO: Maybe we should add different char than * here?
                keys.add(URIAuthorizationPolicy.WILDCARD);
            }
        }

        // Add last key for action
        String action = req.getRequestType().name();
        keys.add(action);

        // Look for best RolePolicy
        RolePolicy rolePolicy = permissions.recursiveGet(keys);

        // Find applicationName from persister, so we can obtain applicationRoles for correct application from token
        String appId = AuthServicesHolder.getInstance().getApplicationIdResolver().resolveAppId(req);
        String appName = AuthServicesHolder.getInstance().getAuthPersister().getApplicationMetadata(appId).getApplicationName();

        AuthorizationDecision authDecision = checkPermissions(rolePolicy, authRequestContext, appName);
        return authDecision;
    }

    public void addRolePolicy(String type, String collectionName, String resourceId, String action, RolePolicy policy) {
        Deque<String> keys = new LinkedList<>();
        keys.add(type);
        keys.add(collectionName);
        keys.add(resourceId);
        keys.add(action);
        permissions.recursivePut(keys, policy);
    }

    protected AuthorizationDecision checkPermissions(RolePolicy rolePolicy, AuthorizationRequestContext authRequestContext, String applicationName) {

        Set<String> realmRoles = getRealmRoles(authRequestContext);
        Set<String> appRoles = getAppRoles(authRequestContext, applicationName);

        AuthorizationDecision realmRolesAuthDecision = rolePolicy.isRealmRolesAllowed(realmRoles);
        AuthorizationDecision appRolesDecision = rolePolicy.isApplicationRolesAllowed(appRoles);

        return mergeDecisions(realmRolesAuthDecision, appRolesDecision);
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

    protected AuthorizationDecision mergeDecisions(AuthorizationDecision authDec1, AuthorizationDecision authDec2) {
        if (authDec1 == AuthorizationDecision.REJECT || authDec2 == AuthorizationDecision.REJECT) {
            return AuthorizationDecision.REJECT;
        } else if (authDec1 == AuthorizationDecision.ACCEPT || authDec2 == AuthorizationDecision.ACCEPT) {
            return AuthorizationDecision.ACCEPT;
        } else {
            return AuthorizationDecision.IGNORE;
        }
    }
}
