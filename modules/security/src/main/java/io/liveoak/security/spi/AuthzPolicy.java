/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.spi;

import io.liveoak.spi.RequestContext;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface AuthzPolicy {
    /**
     * Decide if request is authorized, not authorized or if we don't know
     *
     * @param requestContext encapsulates all info about current request, token etc
     * @return true if request is authorized
     */
    AuthzDecision isAuthorized(RequestContext requestContext);
}
