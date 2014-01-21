package io.liveoak.security.spi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class AuthzPolicyGroup {

    public AuthzPolicyGroup() {
        this.entries = new ArrayList<>();
    }

    public AuthzPolicyGroup(List<AuthzPolicyEntry> entries) {
        this.entries = entries;
    }

    public List<AuthzPolicyEntry> entries() {
        return this.entries;
    }

    public void entries(List<AuthzPolicyEntry> entries) {
        this.entries = entries;
    }

    private List<AuthzPolicyEntry> entries;

}
