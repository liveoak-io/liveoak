/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.uri.integration;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyConfigRule {

    private int priority;
    private String uriPattern;
    private String queryParamsCondition;
    private String requestType;

    private String allowedRoles;
    private String deniedRoles;
    private String allowedUsers;
    private String deniedUsers;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getUriPattern() {
        return uriPattern;
    }

    public void setUriPattern(String uriPattern) {
        this.uriPattern = uriPattern;
    }

    public String getQueryParamsCondition() {
        return queryParamsCondition;
    }

    public void setQueryParamsCondition(String queryParamsCondition) {
        this.queryParamsCondition = queryParamsCondition;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(String allowedRoles) {
        this.allowedRoles = allowedRoles;
    }

    public String getDeniedRoles() {
        return deniedRoles;
    }

    public void setDeniedRoles(String deniedRoles) {
        this.deniedRoles = deniedRoles;
    }

    public String getAllowedUsers() {
        return allowedUsers;
    }

    public void setAllowedUsers(String allowedUsers) {
        this.allowedUsers = allowedUsers;
    }

    public String getDeniedUsers() {
        return deniedUsers;
    }

    public void setDeniedUsers(String deniedUsers) {
        this.deniedUsers = deniedUsers;
    }

    @Override
    public String toString() {
        return new StringBuilder("URIPolicyConfigRule [ ")
                .append("priority=").append(priority)
                .append(", uriPattern=").append(uriPattern)
                .append(", queryParamsCondition=").append(queryParamsCondition)
                .append(", requestType=").append(requestType)
                .append(", allowedRoles=").append(allowedRoles)
                .append(", deniedRoles=").append(deniedRoles)
                .append(", allowedUsers=").append(allowedUsers)
                .append(", deniedUsers=").append(deniedUsers)
                .append(" ]").toString();
    }
}
