package org.projectodd.restafari.container.auth.impl;

import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.container.auth.spi.*;

import java.util.List;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PolicyBasedAuthorizationService implements AuthorizationService {

    SimpleLogger log = new SimpleLogger(PolicyBasedAuthorizationService.class);

    @Override
    public boolean isAuthorized(AuthorizationRequestContext authRequestContext) {
        boolean someSuccess = false;
        ResourceRequest request = authRequestContext.getRequest();

        // Find all policies for particular application
        String appId = AuthServicesHolder.getInstance().getApplicationIdResolver().resolveAppId(request);
        List<AuthorizationPolicyEntry> policies = AuthServicesHolder.getInstance().getAuthPersister().getRegisteredPolicies(appId);

        if (policies.size() == 0) {
            throw new IllegalStateException("No policies configured for application " + appId);
        }

        for (AuthorizationPolicyEntry policyEntry : policies) {
            ResourcePath resPath = request.resourcePath();

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
