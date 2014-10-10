package io.liveoak.common.security;

import io.liveoak.spi.security.SecurityContext;
import io.liveoak.spi.security.UserProfile;

import java.util.Set;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class DefaultSecurityContext implements SecurityContext {

    private String realm;
    private String subject;
    private Set<String> roles;
    private long lastVerified;
    private String original;
    private UserProfile userProfile;

    public DefaultSecurityContext() {
    }

    @Override
    public boolean isAuthenticated() {
        return realm != null && subject != null;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public long lastVerified() {
        return lastVerified;
    }

    public void setLastVerified(long lastVerified) {
        this.lastVerified = lastVerified;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    @Override
    public String getToken() {
        return original;
    }

    @Override
    public UserProfile getUser() {
        return userProfile;
    }

    public void setUser(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public void setOriginal(String original) {
        this.original = original;
    }
}
