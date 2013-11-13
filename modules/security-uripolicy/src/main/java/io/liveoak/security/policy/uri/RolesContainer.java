package io.liveoak.security.policy.uri;

import io.liveoak.security.spi.AuthToken;
import io.liveoak.security.spi.AuthorizationDecision;

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

    private Set<String> allowedRealmRoles;
    private Set<String> allowedApplicationRoles;
    private Set<String> deniedRealmRoles;
    private Set<String> deniedApplicationRoles;
    private Set<String> allowedUsers;
    private Set<String> deniedUsers;

    // METHODS FOR ADDING ROLES INTO CONTAINER

    public RolesContainer addAllowedRealmRole(String roleName) {
        if (allowedRealmRoles == null) {
            allowedRealmRoles = new HashSet<>();
        }
        allowedRealmRoles.add(roleName);
        return this;
    }

    public RolesContainer addAllowedApplicationRole(String roleName) {
        if (allowedApplicationRoles == null) {
            allowedApplicationRoles = new HashSet<>();
        }
        allowedApplicationRoles.add(roleName);
        return this;
    }

    public RolesContainer addDeniedRealmRole(String roleName) {
        if (deniedRealmRoles == null) {
            deniedRealmRoles = new HashSet<>();
        }
        deniedRealmRoles.add(roleName);
        return this;
    }

    public RolesContainer addDeniedApplicationRole(String roleName) {
        if (deniedApplicationRoles == null) {
            deniedApplicationRoles = new HashSet<>();
        }
        deniedApplicationRoles.add(roleName);
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

    public RolesContainer addAllAllowedRealmRoles(Collection<String> coll) {
        if (allowedRealmRoles == null) {
            allowedRealmRoles = new HashSet<>();
        }
        allowedRealmRoles.addAll(coll);
        return this;
    }

    public RolesContainer addAllAllowedApplicationRoles(Collection<String> coll) {
        if (allowedApplicationRoles == null) {
            allowedApplicationRoles = new HashSet<>();
        }
        allowedApplicationRoles.addAll(coll);
        return this;
    }

    public RolesContainer addAllDeniedRealmRoles(Collection<String> coll) {
        if (deniedRealmRoles == null) {
            deniedRealmRoles = new HashSet<>();
        }
        deniedRealmRoles.addAll(coll);
        return this;
    }

    public RolesContainer addAllDeniedApplicationRoles(Collection<String> coll) {
        if (deniedApplicationRoles == null) {
            deniedApplicationRoles = new HashSet<>();
        }
        deniedApplicationRoles.addAll(coll);
        return this;
    }

    public RolesContainer addAllAllowedUsers(Collection<String> coll) {
        if (allowedUsers == null) {
            allowedUsers = new HashSet<>();
        }
        allowedUsers.addAll(coll);
        return this;
    }

    public RolesContainer addAllDeniedUsers(Collection<String> coll) {
        if (deniedUsers == null) {
            deniedUsers = new HashSet<>();
        }
        deniedUsers.addAll(coll);
        return this;
    }

    // GETTERS

    public Set<String> getAllowedRealmRoles() {
        return Collections.unmodifiableSet(allowedRealmRoles);
    }

    public Set<String> getAllowedApplicationRoles() {
        return Collections.unmodifiableSet(allowedApplicationRoles);
    }

    public Set<String> getDeniedRealmRoles() {
        return Collections.unmodifiableSet(deniedRealmRoles);
    }

    public Set<String> getDeniedApplicationRoles() {
        return Collections.unmodifiableSet(deniedApplicationRoles);
    }

    public Set<String> getAllowedUsers() {
        return Collections.unmodifiableSet(allowedUsers);
    }

    public Set<String> getDeniedUsers() {
        return Collections.unmodifiableSet(deniedUsers);
    }

    // CHECKS

    public AuthorizationDecision isRealmRoleAllowed(String roleName) {
        if (deniedRealmRoles != null && (deniedRealmRoles.contains(roleName) || deniedRealmRoles.contains("*"))) {
            return AuthorizationDecision.REJECT;
        } else if (allowedRealmRoles != null && (allowedRealmRoles.contains(roleName) || allowedRealmRoles.contains("*"))) {
            return AuthorizationDecision.ACCEPT;
        }

        return AuthorizationDecision.IGNORE;
    }

    public AuthorizationDecision isApplicationRoleAllowed(String roleName) {
        if (deniedApplicationRoles != null && (deniedApplicationRoles.contains(roleName) || deniedRealmRoles.contains("*"))) {
            return AuthorizationDecision.REJECT;
        } else if (allowedApplicationRoles != null && (allowedApplicationRoles.contains(roleName) || allowedApplicationRoles.contains("*"))) {
            return AuthorizationDecision.ACCEPT;
        }

        return AuthorizationDecision.IGNORE;
    }

    public AuthorizationDecision isRealmRolesAllowed(Collection<String> roles) {
        boolean anyAllowed = false;
        for (String role : roles) {
            AuthorizationDecision authDecision = isRealmRoleAllowed(role);
            if (authDecision == AuthorizationDecision.REJECT) {
                // REJECT always wins
                return AuthorizationDecision.REJECT;
            } else if (authDecision == AuthorizationDecision.ACCEPT) {
                anyAllowed = true;
            }
        }

        return anyAllowed ? AuthorizationDecision.ACCEPT : AuthorizationDecision.IGNORE;
    }

    public AuthorizationDecision isApplicationRolesAllowed(Collection<String> roles) {
        boolean anyAllowed = false;
        for (String role : roles) {
            AuthorizationDecision authDecision = isApplicationRoleAllowed(role);
            if (authDecision == AuthorizationDecision.REJECT) {
                // REJECT always wins
                return AuthorizationDecision.REJECT;
            } else if (authDecision == AuthorizationDecision.ACCEPT) {
                anyAllowed = true;
            }
        }

        return anyAllowed ? AuthorizationDecision.ACCEPT : AuthorizationDecision.IGNORE;
    }

    public AuthorizationDecision isTokenAllowed(AuthToken token) {
        AuthorizationDecision realmDecision = isRealmRolesAllowed(token.getRealmRoles());
        AuthorizationDecision appRolesDecision = isApplicationRolesAllowed(token.getApplicationRoles());
        AuthorizationDecision usernameDecision = isUserAllowed(token.getUsername());
        return realmDecision.mergeDecision(appRolesDecision).mergeDecision(usernameDecision);
    }

    public AuthorizationDecision isUserAllowed(String username) {
        if (deniedUsers != null && (deniedUsers.contains(username) || deniedUsers.contains("*"))) {
            return AuthorizationDecision.REJECT;
        } else if (allowedUsers != null && ((allowedUsers.contains(username)) || allowedUsers.contains("*"))) {
            return AuthorizationDecision.ACCEPT;
        }

        return AuthorizationDecision.IGNORE;
    }

    // HELPER METHODS

    @Override
    public String toString() {
        return new StringBuilder("RolesContainer [ allowedRealmRoles=")
                .append(allowedRealmRoles)
                .append(", allowedApplicationRoles=")
                .append(allowedApplicationRoles)
                .append(", deniedRealmRoles=")
                .append(deniedRealmRoles)
                .append(", deniedApplicationRoles=")
                .append(deniedApplicationRoles)
                .append(", allowedUsers=")
                .append(allowedUsers)
                .append(", deniedUsers=")
                .append(deniedUsers)
                .append(" ]").toString();
    }
}
