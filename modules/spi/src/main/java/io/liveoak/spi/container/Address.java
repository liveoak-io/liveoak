package io.liveoak.spi.container;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface Address {

    void host(String host);
    String host();

    void port(int port);
    int port();

    void portUndertow(int portUndertow);
    int portUndertow();

}
