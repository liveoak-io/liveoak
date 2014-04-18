/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri;

import io.liveoak.common.security.AuthzDecision;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.SecurityContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds info about allowed and denied roles and users for particular policy entry.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RolesContainer {

    private Set<String> allowedRoles;
    private Set<String> deniedRoles;
    private Set<String> allowedUsers;
    private Set<String> deniedUsers;

    // METHODS FOR ADDING ROLES INTO CONTAINER

    public RolesContainer addAllowedRole(String roleName) {
        if (allowedRoles == null) {
            allowedRoles = new HashSet<>();
        }
        allowedRoles.add(roleName);
        return this;
    }

    public RolesContainer addDeniedRole(String roleName) {
        if (deniedRoles == null) {
            deniedRoles = new HashSet<>();
        }
        deniedRoles.add(roleName);
        return this;
    }

    public RolesContainer addAllowedUser(String username) {
        if (allowedUsers == null) {
            allowedUsers = new HashSet<>();
        }
        allowedUsers.add(username);
        return this;
    }

    public RolesContainer addDeniedUser(String username) {
        if (deniedUsers == null) {
            deniedUsers = new HashSet<>();
        }
        deniedUsers.add(username);
        return this;
    }

    public RolesContainer addAllAllowedRoles(Collection<String> coll) {
        if (coll == null) {
            return this;
        }

        if (allowedRoles == null) {
            allowedRoles = new HashSet<>();
        }
        allowedRoles.addAll(coll);
        return this;
    }

    public RolesContainer addAllDeniedRoles(Collection<String> coll) {
        if (coll == null) {
            return this;
        }

        if (deniedRoles == null) {
            deniedRoles = new HashSet<>();
        }
        deniedRoles.addAll(coll);
        return this;
    }

    public RolesContainer addAllAllowedUsers(Collection<String> coll) {
        if (coll == null) {
            return this;
        }

        if (allowedUsers == null) {
            allowedUsers = new HashSet<>();
        }
        allowedUsers.addAll(coll);
        return this;
    }

    public RolesContainer addAllDeniedUsers(Collection<String> coll) {
        if (coll == null) {
            return this;
        }

        if (deniedUsers == null) {
            deniedUsers = new HashSet<>();
        }
        deniedUsers.addAll(coll);
        return this;
    }

    // GETTERS

    public Set<String> getAllowedRoles() {
        return Collections.unmodifiableSet(allowedRoles);
    }

    public Set<String> getDeniedRoles() {
        return Collections.unmodifiableSet(deniedRoles);
    }

    public Set<String> getAllowedUsers() {
        return Collections.unmodifiableSet(allowedUsers);
    }

    public Set<String> getDeniedUsers() {
        return Collections.unmodifiableSet(deniedUsers);
    }

    // CHECKS

    public AuthzDecision isRoleAllowed(String roleName) {
        if (deniedRoles != null && (deniedRoles.contains(roleName) || deniedRoles.contains("*"))) {
            return AuthzDecision.REJECT;
        } else if (allowedRoles != null && (allowedRoles.contains(roleName) || allowedRoles.contains("*"))) {
            return AuthzDecision.ACCEPT;
        }

        return AuthzDecision.IGNORE;
    }

    public AuthzDecision isRolesAllowed(Collection<String> roles) {
        boolean anyAllowed = false;

        // Just to enforce * rule if it's used in allowedRoles or deniedRoles
        if (roles == null) {
            return isRoleAllowed("__PLACEHOLDER__");
        }

        for (String role : roles) {
            AuthzDecision authDecision = isRoleAllowed(role);
            if (authDecision == AuthzDecision.REJECT) {
                // REJECT always wins
                return AuthzDecision.REJECT;
            } else if (authDecision == AuthzDecision.ACCEPT) {
                anyAllowed = true;
            }
        }

        return anyAllowed ? AuthzDecision.ACCEPT : AuthzDecision.IGNORE;
    }

    public AuthzDecision isRequestAllowed(RequestContext req) {
        SecurityContext secCtx = req.securityContext();
        AuthzDecision roleDecision = isRolesAllowed(secCtx.getRoles());
        AuthzDecision usernameDecision = isUserAllowed(secCtx.getSubject());
        return roleDecision.mergeDecision(usernameDecision);
    }

    public AuthzDecision isUserAllowed(String username) {
        if (deniedUsers != null && (deniedUsers.contains(username) || deniedUsers.contains("*"))) {
            return AuthzDecision.REJECT;
        } else if (allowedUsers != null && ((allowedUsers.contains(username)) || allowedUsers.contains("*"))) {
            return AuthzDecision.ACCEPT;
        }

        return AuthzDecision.IGNORE;
    }

    // HELPER METHODS

    @Override
    public String toString() {
        return new StringBuilder("RolesContainer [ allowedRoles=")
                .append(allowedRoles)
                .append(", deniedRoles=")
                .append(deniedRoles)
                .append(", allowedUsers=")
                .append(allowedUsers)
                .append(", deniedUsers=")
                .append(deniedUsers)
                .append(" ]").toString();
    }
}
