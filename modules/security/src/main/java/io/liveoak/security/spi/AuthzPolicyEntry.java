/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.spi;


import java.util.List;

import io.liveoak.spi.ResourcePath;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzPolicyEntry {

    private String policyName;
    private List<String> includedResourcePrefixes;
    private List<String> excludedResourcePrefixes;
    private String policyResourceEndpoint;

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public List<String> getIncludedResourcePrefixes() {
        return includedResourcePrefixes;
    }

    public void setIncludedResourcePrefixes(List<String> includedResourcePrefixes) {
        this.includedResourcePrefixes = includedResourcePrefixes;
    }

    public List<String> getExcludedResourcePrefixes() {
        return excludedResourcePrefixes;
    }

    public void setExcludedResourcePrefixes(List<String> excludedResourcePrefixes) {
        this.excludedResourcePrefixes = excludedResourcePrefixes;
    }

    public String getPolicyResourceEndpoint() {
        return policyResourceEndpoint;
    }

    public void setPolicyResourceEndpoint(String policyResourceEndpoint) {
        this.policyResourceEndpoint = policyResourceEndpoint;
    }

    @Override
    public String toString() {
        return new StringBuilder("AuthzPolicyEntry [ ")
                .append("policyName=").append(policyName)
                .append(", includedResourcePrefixes=").append(includedResourcePrefixes)
                .append(", excludedResourcePrefixes=").append(excludedResourcePrefixes)
                .append(", policyResourceEndpoint=").append(policyResourceEndpoint)
                .append(" ]").toString();
    }


    /**
     * Check if resourcePath is subject of this policy according to includedResourcePrefixes and excludedResourcePrefixes
     *
     * @param resourcePath
     * @return true if resource is subject of this policy
     */
    public boolean isResourceMapped(ResourcePath resourcePath) {
        String resPathString = resourcePath.toString();

        // ResourcePath of root resource is not "/" but ""
        if (resPathString.isEmpty()) {
            resPathString = "/";
        }

        if (includedResourcePrefixes == null && excludedResourcePrefixes == null) {
            return true;
        }

        // Check excluded first
        if (excludedResourcePrefixes != null) {
            for (String current : excludedResourcePrefixes) {
                if (resPathString.startsWith(current)) {
                    return false;
                }
            }
        }

        if (includedResourcePrefixes != null) {
            for (String current : includedResourcePrefixes) {
                if (resPathString.startsWith(current.toString())) {
                    return true;
                }
            }
        }

        return false;
    }
}
