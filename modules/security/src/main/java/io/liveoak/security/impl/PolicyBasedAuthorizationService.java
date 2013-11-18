/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.impl;

import io.liveoak.security.spi.AuthorizationDecision;
import io.liveoak.security.spi.AuthorizationPolicy;
import io.liveoak.security.spi.AuthorizationPolicyEntry;
import io.liveoak.security.spi.AuthorizationRequestContext;
import io.liveoak.security.spi.AuthorizationService;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;

import java.util.List;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PolicyBasedAuthorizationService implements AuthorizationService {

    SimpleLogger log = new SimpleLogger( PolicyBasedAuthorizationService.class );

    @Override
    public boolean isAuthorized( AuthorizationRequestContext authRequestContext ) {
        boolean someSuccess = false;
        RequestContext request = authRequestContext.getRequestContext();

        // Find all policies for particular application
        String appId = AuthServicesHolder.getInstance().getApplicationIdResolver().resolveAppId( request );
        List<AuthorizationPolicyEntry> policies = AuthServicesHolder.getInstance().getAuthPersister().getRegisteredPolicies( appId );

        if ( policies.size() == 0 ) {
            throw new IllegalStateException( "No policies configured for application " + appId );
        }

        for ( AuthorizationPolicyEntry policyEntry : policies ) {
            ResourcePath resPath = request.getResourcePath();

            // Check if policy is mapped to actual resourcePath
            if ( policyEntry.isResourceMapped( resPath ) ) {
                AuthorizationPolicy policy = policyEntry.getAuthorizationPolicy();

                if ( log.isTraceEnabled() ) {
                    log.trace( "Going to trigger policy for request: " + request + ", policyEntry: " + policyEntry );
                }
                AuthorizationDecision decision = policy.isAuthorized( authRequestContext );
                if ( log.isTraceEnabled() ) {
                    log.trace( "Result of authorization policy check: " + decision );
                }

                if ( decision == AuthorizationDecision.REJECT ) {
                    // reject always wins
                    return false;
                } else if ( decision == AuthorizationDecision.ACCEPT ) {
                    someSuccess = true;
                }
            }
        }

        return someSuccess;
    }
}
