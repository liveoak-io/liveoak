/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.impl;

import io.liveoak.security.spi.AuthToken;
import io.liveoak.spi.SecurityContext;

import java.security.Principal;
import java.util.Set;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultSecurityContext implements SecurityContext {

    private final Principal principal;
    private Set<String> realmRoles;
    private Set<String> applicationRoles;

    private DefaultSecurityContext( Principal principal, Set<String> realmRoles, Set<String> appRoles ) {
        this.principal = principal;
        this.realmRoles = realmRoles;
        this.applicationRoles = appRoles;
    }

    public static SecurityContext createFromAuthToken( final AuthToken authToken ) {
        if ( authToken.isAnonymous() ) {
            return SecurityContext.ANONYMOUS;
        } else {
            Principal principal = () -> authToken.getUsername();
            return new DefaultSecurityContext( principal, authToken.getRealmRoles(), authToken.getApplicationRoles() );
        }
    }


    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public Set<String> getRealmRoles() {
        return realmRoles;
    }

    @Override
    public Set<String> getApplicationRoles() {
        return applicationRoles;
    }
}
