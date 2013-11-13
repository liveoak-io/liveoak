package org.projectodd.restafari.security.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.projectodd.restafari.security.spi.AuthToken;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultAuthToken implements AuthToken {

    private final String username;
    private final String realmName;
    private final String applicationName;
    private final long expiration;
    private final long notBefore;
    private final long issuedAt;
    private final String issuer;
    private final Set<String> realmRoles;
    private final Map<String, Set<String>> applicationRolesMap;

    public DefaultAuthToken(String username, String realmName, String applicationName, long expiration, long notBefore, long issuedAt,
                            String issuer, Set<String> realmRoles, Map<String, Set<String>> applicationRolesMap) {
        this.username = username;
        this.realmName = realmName;
        this.applicationName = applicationName;
        this.expiration = expiration;
        this.notBefore = notBefore;
        this.issuedAt = issuedAt;
        this.issuer = issuer;
        this.realmRoles = realmRoles;
        this.applicationRolesMap = applicationRolesMap;
    }

    public DefaultAuthToken(JsonWebToken internalToken) {
        JsonWebToken.Claims claims = internalToken.getClaims();
        this.username = claims.getSubject();
        this.realmName = claims.getAudience();
        this.applicationName = claims.getIssuedFor();
        this.expiration = claims.getExpiration();
        this.notBefore = claims.getNotBefore();
        this.issuedAt = claims.getIssuedAt();
        this.issuer = claims.getIssuer();

        JsonWebToken.Access realmAccess = claims.getRealmAccess();
        this.realmRoles = realmAccess!=null ? Collections.unmodifiableSet(realmAccess.getRoles()) : Collections.EMPTY_SET;

        Map<String, JsonWebToken.Access> appAccess = claims.getResourceAccess();
        if (appAccess == null) {
            this.applicationRolesMap = Collections.EMPTY_MAP;
        } else {
            Map<String, Set<String>> applicationRolesMap = new HashMap<>();
            for (Map.Entry<String, JsonWebToken.Access> entry : appAccess.entrySet()) {
                Set<String> appRoles =  entry.getValue()!=null ? entry.getValue().getRoles() : Collections.EMPTY_SET;
                applicationRolesMap.put(entry.getKey(), appRoles);
            }
            this.applicationRolesMap = Collections.unmodifiableMap(applicationRolesMap);
        }
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getRealmName() {
        return realmName;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public long getExpiration() {
        return expiration;
    }

    @Override
    public long getNotBefore() {
        return notBefore;
    }

    @Override
    public long getIssuedAt() {
        return issuedAt;
    }

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public Set<String> getRealmRoles() {
        return realmRoles;
    }

    @Override
    public Map<String, Set<String>> getApplicationRolesMap() {
        return applicationRolesMap;
    }

    @Override
    public String toString() {
        return new StringBuilder("DefaultAuthToken [ username=").append(username)
                .append(", realmRoles=").append(realmRoles)
                .append(", applicationRoles=").append(applicationRolesMap)
                .append(" ]")
                .toString();
    }
}
