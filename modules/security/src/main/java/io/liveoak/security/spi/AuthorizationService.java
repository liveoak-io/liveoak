/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.spi;

/**
 * Service providing authorization
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface AuthorizationService {

    /**
     * Decide if request is authorized or not
     *
     * @param authRequestContext encapsulates all info about current request, token etc
     * @return true if request is authorized
     */
    boolean isAuthorized( AuthorizationRequestContext authRequestContext );
}
