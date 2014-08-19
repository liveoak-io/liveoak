package io.liveoak.spi;

/**
 * @author Ken Finnigan
 */
public interface ApplicationClient {
    String id();
    String type();
    String securityKey();
}
