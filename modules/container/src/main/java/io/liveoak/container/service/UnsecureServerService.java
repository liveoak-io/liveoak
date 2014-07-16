package io.liveoak.container.service;

import io.liveoak.container.server.AbstractNetworkServer;
import io.liveoak.container.server.UnsecureServer;

/**
 * @author Bob McWhirter
 */
public class UnsecureServerService extends AbstractNetworkServerService {

    @Override
    public AbstractNetworkServer newServer() {
        return new UnsecureServer();
    }
}
