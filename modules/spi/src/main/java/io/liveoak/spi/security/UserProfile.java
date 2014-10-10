package io.liveoak.spi.security;

/**
 * Holds info about user.
 *
 * @author Ken Finnigan
 */
public interface UserProfile {

    /**
     * @return name of user.
     */
    String name();

    /**
     * @return givenName of user.
     */
    String givenName();

    /**
     * @return familyName of user.
     */
    String familyName();

    /**
     * @return email address of user.
     */
    String email();

}
