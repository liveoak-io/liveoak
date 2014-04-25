package io.liveoak.security.policy.acl.impl;

import java.util.List;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyConfig {

    private List<AutoRuleConfig> autoRules;

    public List<AutoRuleConfig> getAutoRules() {
        return autoRules;
    }

    public void setAutoRules(List<AutoRuleConfig> autoRules) {
        this.autoRules = autoRules;
    }

    @Override
    public String toString() {
        return new StringBuilder("AclPolicyConfig [ ")
                .append("autoRules=").append(autoRules)
                .append(" ]").toString();
    }
}
