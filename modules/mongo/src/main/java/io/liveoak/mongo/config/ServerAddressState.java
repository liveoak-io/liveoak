package io.liveoak.mongo.config;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.ServerAddress;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ServerAddressState extends EmbeddedConfigResource {
    ServerAddress serverAddress;

    public static final String HOST = "host";
    public static final String PORT = "port";

    public ServerAddressState(Resource parent, ResourceState resourceState) throws Exception {
        super(parent);
        String host = resourceState.getProperty(HOST, false, String.class);
        Integer port = resourceState.getProperty(PORT, false, Integer.class);

        if (port == null) {
            serverAddress = new ServerAddress(host);
        } else {
            serverAddress = new ServerAddress(host, port);
        }
    }

    public ServerAddressState(Resource parent, ServerAddress serverAddress) {
        super(parent);
        this.serverAddress = serverAddress;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map properties = new HashMap<>();
        properties.put(HOST, serverAddress.getHost());
        properties.put(PORT, serverAddress.getPort());
        return properties;
    }

    public ServerAddress getServerAddress() {
        return serverAddress;
    }
}
