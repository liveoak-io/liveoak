package org.projectodd.restafari.security.spi;

import org.projectodd.restafari.spi.RequestContext;

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
