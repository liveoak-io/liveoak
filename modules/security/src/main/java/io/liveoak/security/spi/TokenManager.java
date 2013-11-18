/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.spi;

import io.liveoak.spi.RequestContext;

/**
 * Component used to parse {@link io.liveoak.security.impl.JsonWebToken} from request and validate this token.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface TokenManager {

    AuthToken getAndValidateToken( RequestContext requestContext ) throws TokenValidationException;
}
