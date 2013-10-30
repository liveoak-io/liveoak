package org.projectodd.restafari.container.auth.spi;

import org.projectodd.restafari.container.ResourceRequest;

/**
 * Encapsulates all data needed for authorization decision
 *
 * @see AuthorizationService
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthorizationRequestContext {

    private final JsonWebToken accessToken;
    private final ResourceRequest resourceRequest;

    public AuthorizationRequestContext(JsonWebToken accessToken, ResourceRequest resourceRequest) {
        this.resourceRequest = resourceRequest;
        this.accessToken = accessToken;
    }

    public JsonWebToken getAccessToken() {
        return accessToken;
    }

    public ResourceRequest getRequest() {
        return resourceRequest;
    }

    public boolean isRequestAuthenticated() {
        return getAccessToken() != null;
    }
}
