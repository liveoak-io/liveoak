/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.drools.integration;

import java.util.List;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DroolsPolicyConfig {

    private List<DroolsPolicyConfigRule> rules;

    public List<DroolsPolicyConfigRule> getRules() {
        return rules;
    }

    public void setRules(List<DroolsPolicyConfigRule> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return new StringBuilder("DroolsPolicyConfig [ ")
                .append("rules=").append(rules)
                .append(" ]").toString();
    }
}
