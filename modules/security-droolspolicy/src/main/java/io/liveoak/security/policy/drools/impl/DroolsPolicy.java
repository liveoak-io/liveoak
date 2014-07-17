/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.drools.impl;

import java.util.concurrent.atomic.AtomicReference;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.jboss.logging.Logger;

/**
 * Policy for authorization of resources based on drools engine
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DroolsPolicy {

    private static final Logger log = Logger.getLogger(DroolsPolicy.class);

    private AtomicReference<RuleBase> ruleBase = new AtomicReference<>();

    public RuleBase getRuleBase() {
        return ruleBase.get();
    }

    public void setRuleBase(RuleBase ruleBase) {
        this.ruleBase.set(ruleBase);
    }

    public AuthzDecision isAuthorized(RequestContext reqContext) {
        return isAuthorized(reqContext, null, null);
    }

    public AuthzDecision isAuthorized(RequestContext reqContext, ResourceState reqResourceState, ResourceState respResourceState) {
        if (log.isTraceEnabled()) {
            log.trace("Start checking request: " + reqContext);
        }

        WorkingMemory workingMemory = null;

        try {
            workingMemory = getRuleBase().newStatefulSession();

            RulesProcessingResult rulesProcessingResult = new RulesProcessingResult();
            workingMemory.insert(rulesProcessingResult);

            EndSemaphore endSemaphore = new EndSemaphore();
            workingMemory.insert(endSemaphore);

            URIMatcherCache cache = new URIMatcherCache();
            workingMemory.insert(cache);

            // TODO: Verify if it's better to first insert request or securityContext (Rules checking is triggered right after inserting, so it could affect performance)
            RequestContextDecorator reqContextDecorator = new RequestContextDecorator(reqContext);
            workingMemory.insert(reqContextDecorator);
            workingMemory.insert(reqContextDecorator.securityContext());
            workingMemory.insert(reqContextDecorator.resourceParams());

            // TODO: this is temporary. Actually we have either requestResource (in case of CREATE or UPDATE requests) or responseState (in case of outbound or subscription READ requests)
            ResourceState stateToInsert;
            if (reqResourceState != null) {
                stateToInsert = reqResourceState;
            } else if (respResourceState != null) {
                stateToInsert = respResourceState;
            } else {
                stateToInsert = new DefaultResourceState();
            }
            workingMemory.insert(stateToInsert);

            // Uncomment for drools debugging (TODO: should be somehow configurable...)
            //workingMemory.addEventListener(new DebugAgendaEventListener());
            //workingMemory.addEventListener( new DebugWorkingMemoryEventListener() );

            int numberOfFiredPolicies = workingMemory.fireAllRules();

            if (log.isTraceEnabled()) {
                log.trace("Overall result for request: " + reqContext + ". Count of fired policies=" + numberOfFiredPolicies + ", Result=" + rulesProcessingResult.getDecision());
            }
            return rulesProcessingResult.getDecision();
        } finally {
            if (workingMemory != null) {
                workingMemory.dispose();
            }
        }
    }
}
