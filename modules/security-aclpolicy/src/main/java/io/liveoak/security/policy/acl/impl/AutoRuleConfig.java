/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.acl.impl;

import java.util.List;

/**
 * The auto rule. It's used to specify new ACE, which will be created for the owner once he create some resource
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AutoRuleConfig {

    // The path of the resource
    private String resourcePath;

    // List of permissions, which will be automatically created for the owner of the newly created resource
    private List<String> autoAddedOwnerPermissions;

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public List<String> getAutoAddedOwnerPermissions() {
        return autoAddedOwnerPermissions;
    }

    public void setAutoAddedOwnerPermissions(List<String> autoAddedOwnerPermissions) {
        this.autoAddedOwnerPermissions = autoAddedOwnerPermissions;
    }

    @Override
    public String toString() {
        return new StringBuilder("AutoRuleConfig [ ")
                .append(", resourcePath=").append(resourcePath)
                .append(", autoAddedOwnerPermissions=").append(autoAddedOwnerPermissions)
                .append(" ]").toString();
    }
}
