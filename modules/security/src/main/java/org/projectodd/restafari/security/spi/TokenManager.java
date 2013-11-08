package org.projectodd.restafari.security.spi;

import org.projectodd.restafari.spi.RequestContext;

/**
 * Component used to parse {@link JsonWebToken} from request and validate this token.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface TokenManager {

    JsonWebToken getToken(RequestContext resourceRequest);

    void validateToken(RequestContext resourceRequest, JsonWebToken token) throws TokenValidationException;
}
