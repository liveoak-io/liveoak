package io.liveoak.client;

import java.net.SocketAddress;

/**
 * @author Bob McWhirter
 */
public interface Connection {

    void connect(SocketAddress address) throws Exception;
    void close();

    void write(ClientRequest request);
}
