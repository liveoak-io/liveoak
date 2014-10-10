package io.liveoak.common.security;

import io.liveoak.spi.security.UserProfile;

/**
 * @author Ken Finnigan
 */
public class DefaultUserProfile implements UserProfile {
    @Override
    public String name() {
        return this.name;
    }

    public DefaultUserProfile name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String givenName() {
        return this.givenName;
    }

    public DefaultUserProfile givenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    @Override
    public String familyName() {
        return this.familyName;
    }

    public DefaultUserProfile familyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    @Override
    public String email() {
        return this.email;
    }

    public DefaultUserProfile email(String email) {
        this.email = email;
        return this;
    }

    private String name;
    private String givenName;
    private String familyName;
    private String email;
}
