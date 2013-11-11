package org.projectodd.restafari.spi;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

/**
 * Holds info about authenticated user and his roles
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface SecurityContext {

    /**
     * @return principal associated with current user or null if user is anonymous
     */
    Principal getPrincipal();

    /**
     * @return username associated with current user or null if user is anonymous
     */
    default String getUsername() {
        Principal principal = getPrincipal();
        return principal == null ? null : principal.getName();
    }

    /**
     * @return list of realm roles, which are assigned to current user (Realm role is something like global role of user for all applications)
     * or empty set if not realm roles available. Never returns null
     */
    Set<String> getRealmRoles();

    /**
     * @return list of application roles for current application, which are assigned to current user
     * or empty set if not application roles availablefor current application. Never returns null.
     */
    Set<String> getApplicationRoles();

    /**
     * @param roleName
     * @return true if user is member of role roleName
     */
    default boolean isUserInRealmRole(String roleName) {
        return getRealmRoles().contains(roleName);
    }

    /**
     * @param roleName
     * @return true if user is member of role roleName
     */
    default boolean isUserInApplicationRole(String roleName) {
        return getApplicationRoles().contains(roleName);
    }

    public static final SecurityContext ANONYMOUS = new SecurityContext() {

        @Override
        public Principal getPrincipal() {
            return null;
        }

        @Override
        public Set<String> getRealmRoles() {
            return Collections.EMPTY_SET;
        }

        @Override
        public Set<String> getApplicationRoles() {
            return Collections.EMPTY_SET;
        }
    };
}
