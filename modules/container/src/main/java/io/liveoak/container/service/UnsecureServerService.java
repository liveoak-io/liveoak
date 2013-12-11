package io.liveoak.container.service;

import io.liveoak.container.server.AbstractServer;
import io.liveoak.spi.container.Server;
import io.liveoak.container.server.UnsecureServer;

/**
 * @author Bob McWhirter
 */
public class UnsecureServerService extends AbstractServerService {

    @Override
    public AbstractServer newServer() {
        return new UnsecureServer();
    }
}
