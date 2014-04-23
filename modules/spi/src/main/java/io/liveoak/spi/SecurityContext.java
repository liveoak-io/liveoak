/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

import java.util.Collections;
import java.util.Set;

/**
 * Holds info about authenticated user and his roles
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface SecurityContext {

    /**
     * @return true if authenticated
     */
    boolean isAuthenticated();

    /**
     * @return the realm used to authenticate current user or null if not authenticated
     */
    String getRealm();

    /**
     * @return the username of the authenticated user or null if not authenticated
     */
    String getSubject();

    /**
     * @return date when credentials was last verified or -1 if not authenticated
     * */
    long lastVerified();

    /**
     * @return roles assigned to current user or null if not authenticated
     */
    Set<String> getRoles();

    String getToken();

    /**
     * @param role the role
     * @return if the authenticated user is assigned the specified role
     */
    boolean hasRole(String role);

    SecurityContext ANONYMOUS = new SecurityContext() {

        @Override
        public boolean isAuthenticated() {
            return false;
        }

        @Override
        public String getRealm() {
            return null;
        }

        @Override
        public String getSubject() {
            return null;
        }

        @Override
        public long lastVerified() {
            return 0;
        }

        @Override
        public Set<String> getRoles() {
            return Collections.EMPTY_SET;
        }

        @Override
        public boolean hasRole(String role) {
            return false;
        }

        @Override
        public String getToken() {
            return null;
        }

    };

}
