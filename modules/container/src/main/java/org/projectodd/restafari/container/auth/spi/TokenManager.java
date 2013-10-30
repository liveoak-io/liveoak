package org.projectodd.restafari.container.auth.spi;

import org.projectodd.restafari.container.ResourceRequest;

/**
 * Component used to parse {@link JsonWebToken} from request and validate this token.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface TokenManager {

    JsonWebToken getToken(ResourceRequest resourceRequest);

    void validateToken(ResourceRequest resourceRequest, JsonWebToken token) throws TokenValidationException;
}
