package org.projectodd.restafari.security.impl;

import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.ResourcePath;
import org.projectodd.restafari.security.spi.*;

import java.util.List;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PolicyBasedAuthorizationService implements AuthorizationService {

    SimpleLogger log = new SimpleLogger(PolicyBasedAuthorizationService.class);

    @Override
    public boolean isAuthorized(AuthorizationRequestContext authRequestContext) {
        boolean someSuccess = false;
        RequestContext request = authRequestContext.getRequestContext();

        // Find all policies for particular application
        String appId = AuthServicesHolder.getInstance().getApplicationIdResolver().resolveAppId(request);
        List<AuthorizationPolicyEntry> policies = AuthServicesHolder.getInstance().getAuthPersister().getRegisteredPolicies(appId);

        if (policies.size() == 0) {
            throw new IllegalStateException("No policies configured for application " + appId);
        }

        for (AuthorizationPolicyEntry policyEntry : policies) {
            ResourcePath resPath = request.getResourcePath();

            // Check if policy is mapped to actual resourcePath
            if (policyEntry.isResourceMapped(resPath)) {
                AuthorizationPolicy policy = policyEntry.getAuthorizationPolicy();

                if (log.isTraceEnabled()) {
                    log.trace("Going to trigger policy for request: " + request + ", policyEntry: " + policyEntry);
                }
                AuthorizationDecision decision = policy.isAuthorized(authRequestContext);
                if (log.isTraceEnabled()) {
                    log.trace("Result of authorization policy check: " + decision);
                }

                if (decision == AuthorizationDecision.REJECT) {
                    // reject always wins
                    return false;
                }  else if (decision == AuthorizationDecision.ACCEPT) {
                    someSuccess = true;
                }
            }
        }

        return someSuccess;
    }
}
