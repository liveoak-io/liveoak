/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.spi;

import io.liveoak.spi.RequestContext;

/**
 * Encapsulates all data needed for authorization decision
 *
 * @see AuthorizationService
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthorizationRequestContext {

    private final AuthToken authToken;
    private final RequestContext requestContext;

    public AuthorizationRequestContext(AuthToken authToken, RequestContext requestContext) {
        this.requestContext = requestContext;
        this.authToken = authToken;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public boolean isRequestAuthenticated() {
        return !AuthToken.ANONYMOUS_TOKEN.equals(authToken);
    }
}
