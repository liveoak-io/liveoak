/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri.impl;

import java.util.Collection;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyRule {

    private final Collection<String> requestTypes;
    private final RolesContainer rolesContainer;

    public URIPolicyRule(Collection<String> requestTypes, RolesContainer rolesContainer) {
        this.requestTypes = requestTypes;
        this.rolesContainer = rolesContainer;
    }

    public Collection<String> getRequestTypes() {
        return requestTypes;
    }

    public RolesContainer getRolesContainer() {
        return rolesContainer;
    }

    @Override
    public String toString() {
        return new StringBuilder("URIPolicyRule [ requestTypes=")
                .append(requestTypes).append(", rolesContainer=").append(rolesContainer).append(" ]").toString();
    }
}
