package io.liveoak.spi.container;

import java.net.InetAddress;

import io.liveoak.spi.Container;

/**
 * @author Bob McWhirter
 */
public interface Server {

    void host(InetAddress host);
    InetAddress host();

    void port(int port);
    int port();

    void start() throws Exception;
    void stop() throws Exception;
}
