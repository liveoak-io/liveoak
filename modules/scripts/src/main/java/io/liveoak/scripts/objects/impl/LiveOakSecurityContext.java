package io.liveoak.scripts.objects.impl;


import java.util.ArrayList;
import java.util.List;

import io.liveoak.scripts.objects.SecurityContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakSecurityContext implements SecurityContext {

    io.liveoak.spi.SecurityContext securityContext;

    public LiveOakSecurityContext(io.liveoak.spi.SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    @Override
    public Boolean getAuthenticated() {
        return securityContext.isAuthenticated();
    }

    @Override
    public String getRealm() {
        return securityContext.getRealm();
    }

    @Override
    public String getSubject() {
        return securityContext.getSubject();
    }

    @Override
    public Long getLastVerified() {
        return securityContext.lastVerified();
    }

    @Override
    public List<String> getRoles() {
        if (securityContext != null && securityContext.getRoles() != null) {
            return new ArrayList<>(securityContext.getRoles());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public String getToken() {
        return securityContext.getToken();
    }

    @Override
    public Boolean hasRole(String role) {
        return securityContext.hasRole(role);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("authenticated :").append(getAuthenticated());
        builder.append(", realm: " + getRealm());
        builder.append(", subject: " + getSubject());
        builder.append(", last-verified: " + getLastVerified());
        builder.append(", token: " + getToken());
        builder.append(", realm: " + getRoles());

        builder.append("}");
        return builder.toString();
    }
}
