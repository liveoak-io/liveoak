package io.liveoak.security.policy.acl;

import java.util.List;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyConfig {

    private List<AclPolicyConfigRule> aclRules;

    public List<AclPolicyConfigRule> getAclRules() {
        return aclRules;
    }

    public void setAclRules(List<AclPolicyConfigRule> aclRules) {
        this.aclRules = aclRules;
    }

    @Override
    public String toString() {
        return new StringBuilder("AclPolicyConfig [ ")
                .append("aclRules=").append(aclRules)
                .append(" ]").toString();
    }
}
