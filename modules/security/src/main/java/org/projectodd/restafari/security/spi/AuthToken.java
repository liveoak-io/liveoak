package org.projectodd.restafari.security.spi;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates info about authenticated token, which are important for authorization
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface AuthToken {

    String getUsername();

    String getRealmName();

    String getApplicationName();

    long getExpiration();

    long getNotBefore();

    long getIssuedAt();

    String getIssuer();

    /**
     * @return set with all granted realm (global) roles or empty set if no realm roles granted. Never returns null
     */
    Set<String> getRealmRoles();

    /**
     * @return map with all granted application roles mapping or empty map if no application roles granted. Never returns null
     */
    Map<String, Set<String>> getApplicationRolesMap();

    /**
     * @return set with all granted roles for current application or empty set if no roles granted for current app. Never returns null
     */
    default Set<String> getApplicationRoles() {
        String applicationName = getApplicationName();
        Set<String> appRoles = getApplicationRolesMap().get(applicationName);
        return appRoles != null ? appRoles : Collections.EMPTY_SET;
    }

    default boolean isAnonymous() {
        return getUsername() == null;
    }

    public static final AuthToken ANONYMOUS_TOKEN = new AuthToken() {

        @Override
        public String getUsername() {
            return null;
        }

        @Override
        public String getRealmName() {
            return null;
        }

        @Override
        public String getApplicationName() {
            return null;
        }

        @Override
        public long getExpiration() {
            return 0;
        }

        @Override
        public long getNotBefore() {
            return 0;
        }

        @Override
        public long getIssuedAt() {
            return 0;
        }

        @Override
        public String getIssuer() {
            return null;
        }

        @Override
        public Set<String> getRealmRoles() {
            return Collections.EMPTY_SET;
        }

        @Override
        public Map<String, Set<String>> getApplicationRolesMap() {
            return Collections.EMPTY_MAP;
        }
    };

}
