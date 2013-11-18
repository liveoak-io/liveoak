/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.spi;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface AuthorizationPolicy {

    /**
     * Perform some needed initialization steps for this policy.
     */
    void init();

    /**
     * Decide if request is authorized, not authorized or if we don't know
     *
     * @param authRequestContext encapsulates all info about current request, token etc
     * @return true if request is authorized
     */
    AuthorizationDecision isAuthorized( AuthorizationRequestContext authRequestContext );
}
