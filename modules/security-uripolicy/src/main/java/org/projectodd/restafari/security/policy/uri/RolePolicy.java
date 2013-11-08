package org.projectodd.restafari.security.policy.uri;

import org.projectodd.restafari.security.spi.AuthorizationDecision;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RolePolicy {

    private final Set<String> allowedRealmRoles;
    private final Set<String> allowedApplicationRoles;
    private final Set<String> deniedRealmRoles;
    private final Set<String> deniedApplicationRoles;

    public RolePolicy() {
        this(null, null, null, null);
    }

    public RolePolicy(Set<String> allowedRealmRoles, Set<String> allowedApplicationRoles, Set<String> deniedRealmRoles, Set<String> deniedApplicationRoles) {
        this.allowedRealmRoles = allowedRealmRoles!=null ? Collections.unmodifiableSet(allowedRealmRoles) : null;
        this.allowedApplicationRoles = allowedApplicationRoles!= null ? Collections.unmodifiableSet(allowedApplicationRoles) : null;
        this.deniedRealmRoles = deniedRealmRoles!=null ? Collections.unmodifiableSet(deniedRealmRoles) : null;
        this.deniedApplicationRoles = deniedApplicationRoles!=null ? Collections.unmodifiableSet(deniedApplicationRoles) : null;
    }

    public AuthorizationDecision isRealmRoleAllowed(String roleName) {
        if (deniedRealmRoles != null && deniedRealmRoles.contains(roleName)) {
            return AuthorizationDecision.REJECT;
        } else if (allowedRealmRoles != null && (allowedRealmRoles.contains(roleName) || allowedRealmRoles.contains(URIAuthorizationPolicy.WILDCARD))) {
            return AuthorizationDecision.ACCEPT;
        }

        return AuthorizationDecision.IGNORE;
    }

    public AuthorizationDecision isApplicationRoleAllowed(String roleName) {
        if (deniedApplicationRoles != null && deniedApplicationRoles.contains(roleName)) {
            return AuthorizationDecision.REJECT;
        } else if (allowedApplicationRoles != null && (allowedApplicationRoles.contains(roleName) || allowedApplicationRoles.contains(URIAuthorizationPolicy.WILDCARD))) {
            return AuthorizationDecision.ACCEPT;
        }

        return AuthorizationDecision.IGNORE;
    }

    public AuthorizationDecision isRealmRolesAllowed(Set<String> roles) {
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

    public AuthorizationDecision isApplicationRolesAllowed(Set<String> roles) {
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

    public Set<String> getAllowedRealmRoles() {
        return allowedRealmRoles;
    }

    public Set<String> getAllowedApplicationRoles() {
        return allowedApplicationRoles;
    }

    public Set<String> getDeniedRealmRoles() {
        return deniedRealmRoles;
    }

    public Set<String> getDeniedApplicationRoles() {
        return deniedRealmRoles;
    }

    public static class RolePolicyBuilder {

        private Set<String> allowedRealmRoles;
        private Set<String> allowedApplicationRoles;
        private Set<String> deniedRealmRoles;
        private Set<String> deniedApplicationRoles;

        public RolePolicyBuilder importRolePolicy(RolePolicy rolePolicy) {
            addAllAllowedRealmRoles(rolePolicy.allowedRealmRoles);
            addAllAllowedApplicationRoles(rolePolicy.allowedApplicationRoles);
            addAllDeniedRealmRoles(rolePolicy.deniedRealmRoles);
            addAllDeniedApplicationRoles(rolePolicy.deniedApplicationRoles);
            return this;
        }

        public RolePolicyBuilder addAllowedRealmRole(String roleName) {
            if (allowedRealmRoles == null) {
                allowedRealmRoles = new HashSet<>();
            }
            allowedRealmRoles.add(roleName);
            return this;
        }

        public RolePolicyBuilder addAllowedApplicationRole(String roleName) {
            if (allowedApplicationRoles == null) {
                allowedApplicationRoles = new HashSet<>();
            }
            allowedApplicationRoles.add(roleName);
            return this;
        }

        public RolePolicyBuilder addDeniedRealmRole(String roleName) {
            if (deniedRealmRoles == null) {
                deniedRealmRoles = new HashSet<>();
            }
            deniedRealmRoles.add(roleName);
            return this;
        }

        public RolePolicyBuilder addDeniedApplicationRole(String roleName) {
            if (deniedApplicationRoles == null) {
                deniedApplicationRoles = new HashSet<>();
            }
            deniedApplicationRoles.add(roleName);
            return this;
        }

        public RolePolicyBuilder addAllAllowedRealmRoles(Collection<String> coll) {
            if (allowedRealmRoles == null) {
                allowedRealmRoles = new HashSet<>();
            }
            allowedRealmRoles.addAll(coll);
            return this;
        }

        public RolePolicyBuilder addAllAllowedApplicationRoles(Collection<String> coll) {
            if (allowedApplicationRoles == null) {
                allowedApplicationRoles = new HashSet<>();
            }
            allowedApplicationRoles.addAll(coll);
            return this;
        }

        public RolePolicyBuilder addAllDeniedRealmRoles(Collection<String> coll) {
            if (deniedRealmRoles == null) {
                deniedRealmRoles = new HashSet<>();
            }
            deniedRealmRoles.addAll(coll);
            return this;
        }

        public RolePolicyBuilder addAllDeniedApplicationRoles(Collection<String> coll) {
            if (deniedApplicationRoles == null) {
                deniedApplicationRoles = new HashSet<>();
            }
            deniedApplicationRoles.addAll(coll);
            return this;
        }

        public RolePolicy build() {
            return new RolePolicy(allowedRealmRoles, allowedApplicationRoles, deniedRealmRoles, deniedApplicationRoles);
        }
    }
}
