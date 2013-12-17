/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.spi;

import java.util.List;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzServiceConfig {

    private List<AuthzPolicyEntry> policies;

    public void setPolicies(List<AuthzPolicyEntry> policies) {
        this.policies = policies;
    }

    public List<AuthzPolicyEntry> getPolicies() {
        return policies;
    }

    @Override
    public String toString() {
        return new StringBuilder("AuthzServiceConfig [ policies=")
                .append(policies)
                .append(" ]").toString();
    }
}
