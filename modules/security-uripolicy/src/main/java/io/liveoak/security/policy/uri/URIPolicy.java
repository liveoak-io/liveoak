/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri;


import io.liveoak.common.security.AuthzDecision;
import io.liveoak.common.util.ObjectsTree;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import org.jboss.logging.Logger;

import java.util.Collection;

/**
 *
 * Simple URI policy, which allows just wildcards (no custom patterns) in ResourcePath segments. Doesn't check request parameters
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicy {

    private static final Logger log = Logger.getLogger(URIPolicy.class);

    private ObjectsTree<URIPolicyRule> rulesTree = new ObjectsTree<>();

    public AuthzDecision isAuthorized(RequestContext req) {
        ResourcePath resourcePath = req.resourcePath();
        DecisionHolder decisionHolder = new DecisionHolder();
        rulesTree.objects(resourcePath).forEach((uriPolicyRule) -> {
            AuthzDecision currentDecision = checkPermissions(uriPolicyRule, req);

            if (log.isTraceEnabled()) {
                log.tracef("Checking resourcePath: %s, rule: %s, result: %s", resourcePath, uriPolicyRule.toString(), currentDecision.toString());
            }

            decisionHolder.decision = decisionHolder.decision.mergeDecision(currentDecision);
        });

        return decisionHolder.decision;
    }

    public void addURIPolicyRule(ResourcePath resourcePath, Collection<String> requestTypes, Collection<String> allowedRoles, Collection<String> deniedRoles,
                                 Collection<String> allowedUsers, Collection<String> deniedUsers) {
        RolesContainer rolesContainer = new RolesContainer()
                .addAllAllowedRoles(allowedRoles).addAllDeniedRoles(deniedRoles)
                .addAllAllowedUsers(allowedUsers).addAllDeniedUsers(deniedUsers);
        URIPolicyRule rule = new URIPolicyRule(requestTypes, rolesContainer);

        this.rulesTree.addObject(rule, resourcePath);

        log.debug("Added new URIPolicyRule for resourcePath " + resourcePath + ". Rule: " + rule);
    }

    protected AuthzDecision checkPermissions(URIPolicyRule uriPolicyRule, RequestContext reqCtx) {
        RequestType reqType = reqCtx.requestType();
        boolean reqTypeMatches = false;
        for (String currentReqType : uriPolicyRule.getRequestTypes()) {
            if (reqType.matches(currentReqType)) {
                reqTypeMatches = true;
                break;
            }
        }

        if (reqTypeMatches) {
            return uriPolicyRule.getRolesContainer().isRequestAllowed(reqCtx);
        } else {
            return AuthzDecision.IGNORE;
        }
    }

    private static class DecisionHolder {
        private AuthzDecision decision = AuthzDecision.IGNORE;
    }
}
