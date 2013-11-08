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

    private final JsonWebToken accessToken;
    private final RequestContext requestContext;

    public AuthorizationRequestContext(JsonWebToken accessToken, RequestContext requestContext) {
        this.requestContext = requestContext;
        this.accessToken = accessToken;
    }

    public JsonWebToken getAccessToken() {
        return accessToken;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public boolean isRequestAuthenticated() {
        return getAccessToken() != null;
    }
}
