package io.liveoak.spi.container;

import java.net.InetAddress;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface Address {

    void host(InetAddress host);
    InetAddress host();

    void port(int port);
    int port();

    void portUndertow(int portUndertow);
    int portUndertow();

}
