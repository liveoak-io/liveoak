package org.projectodd.restafari.security.spi;

import org.projectodd.restafari.spi.RequestContext;

/**
 * Component used to parse {@link org.projectodd.restafari.security.impl.JsonWebToken} from request and validate this token.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface TokenManager {

    AuthToken getAndValidateToken(RequestContext requestContext) throws TokenValidationException;
}
