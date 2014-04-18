package io.liveoak.security.policy.uri.integration;

import java.util.List;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyConfig {

    private List<URIPolicyConfigRule> rules;

    public List<URIPolicyConfigRule> getRules() {
        return rules;
    }

    public void setRules(List<URIPolicyConfigRule> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return new StringBuilder("URIPolicyConfig [ ")
                .append("rules=").append(rules)
                .append(" ]").toString();
    }
}
