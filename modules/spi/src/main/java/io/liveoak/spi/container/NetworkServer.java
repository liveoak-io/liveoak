package io.liveoak.spi.container;

import java.net.InetAddress;

/**
 * @author Bob McWhirter
 */
public interface NetworkServer extends Server {

    void host(InetAddress host);
    InetAddress host();

    void port(int port);
    int port();

    void start() throws Exception;
    void stop() throws Exception;
}
