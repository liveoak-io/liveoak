package io.liveoak.security.policy.uri.integration;

import java.util.List;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyConfigRule {

    private String uriPattern;
    private List<String> requestTypes;
    private List<String> allowedRoles;
    private List<String> deniedRoles;
    private List<String> allowedUsers;
    private List<String> deniedUsers;

    public String getUriPattern() {
        return uriPattern;
    }

    public void setUriPattern(String uriPattern) {
        this.uriPattern = uriPattern;
    }

    public List<String> getRequestTypes() {
        return requestTypes;
    }

    public void setRequestTypes(List<String> requestTypes) {
        this.requestTypes = requestTypes;
    }

    public List<String> getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(List<String> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }

    public List<String> getDeniedRoles() {
        return deniedRoles;
    }

    public void setDeniedRoles(List<String> deniedRoles) {
        this.deniedRoles = deniedRoles;
    }

    public List<String> getAllowedUsers() {
        return allowedUsers;
    }

    public void setAllowedUsers(List<String> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }

    public List<String> getDeniedUsers() {
        return deniedUsers;
    }

    public void setDeniedUsers(List<String> deniedUsers) {
        this.deniedUsers = deniedUsers;
    }
}
