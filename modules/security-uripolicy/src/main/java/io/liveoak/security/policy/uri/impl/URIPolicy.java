/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri.impl;


import io.liveoak.common.security.AuthzDecision;
import io.liveoak.common.util.ObjectsTree;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import org.jboss.logging.Logger;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * Simple URI policy, which allows just wildcards (no custom patterns) in ResourcePath segments. Doesn't check request parameters
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicy {

    private static final Logger log = Logger.getLogger(URIPolicy.class);

    private final AtomicReference<ObjectsTree<URIPolicyRule>> rulesTree = new AtomicReference<>();

    public AuthzDecision isAuthorized(RequestContext req) {
        ResourcePath resourcePath = req.resourcePath();
        DecisionHolder decisionHolder = new DecisionHolder();

        rulesTree.get().objects(resourcePath).forEach((uriPolicyRule) -> {
            ResourcePath currentRuleResourcePath = uriPolicyRule.getResourcePath();
            if (decisionHolder.decision == null || currentRuleResourcePath.equals(decisionHolder.lastResourcePath)) {
                AuthzDecision currentDecision = checkPermissions(uriPolicyRule, req);

                if (log.isTraceEnabled()) {
                    log.tracef("Checking resourcePath: %s, rule: %s, result: %s", resourcePath, uriPolicyRule.toString(), currentDecision.toString());
                }

                decisionHolder.lastResourcePath = currentRuleResourcePath;
                if (decisionHolder.decision == null) {
                    decisionHolder.decision = currentDecision;
                } else {
                    decisionHolder.decision = currentDecision.mergeDecision(decisionHolder.decision);
                }
            }
        });

        return decisionHolder.decision!=null ? decisionHolder.decision : AuthzDecision.IGNORE;
    }

    public void setRulesTree(ObjectsTree<URIPolicyRule> rulesTree) {
        this.rulesTree.set(rulesTree);
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
        private AuthzDecision decision;
        private ResourcePath lastResourcePath;
    }
}
