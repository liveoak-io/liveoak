package io.liveoak.mongo.config;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.MongoClientOptions;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoClientOptionsState extends EmbeddedConfigResource {

    public static final String ID = "MongoClientOptions";

    public static final String DESCRIPTION = "description";
    public static final String CONNECTIONS_PER_HOST="connectionsPerHost";
    public static final String THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER="threadsAllowedToBlockForConnectionMultiplier";
    public static final String MAX_WAIT_TIME="maxWaitTime";
    public static final String CONNECT_TIMEOUT="connectTimeout";
    public static final String SOCKET_KEEP_ALIVE="socketKeepAlive";
    public static final String AUTOCONNECT_RETRY = "autoConnectRetry";
    public static final String MAX_AUTOCONNECT_RETRY_TIME = "maxAutoConnectRetryTime";
    public static final String CURSOR_FINALIZER_ENABLED ="cursorFinalizerEnabled";
    public static final String ALWAYS_USE_MBEANS = "alwaysUseMBeans";

    public static final String DEFAULT_DESCRIPTION = "liveoak"; //TODO: make this configurable

    private MongoClientOptions mongoClientOptions;

    public MongoClientOptionsState(Resource parent, ResourceState state) throws Exception {
        super(parent);
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();

        if (state == null) {
            builder.description(DEFAULT_DESCRIPTION);
            this.mongoClientOptions = builder.build();
            return;
        }

        String description = state.getProperty(DESCRIPTION, false, String.class);
        if (description == null) {
            builder.description(DEFAULT_DESCRIPTION);
        } else {
            builder.description(description);
        }

        Integer connectionsPerHost = state.getProperty(CONNECTIONS_PER_HOST, false, Integer.class);
        if (connectionsPerHost != null) {
            builder.connectionsPerHost(connectionsPerHost);
        }

        Integer threadsAllowedToBlockForConnectionMultiplier = state.getProperty(THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER, false, Integer.class);
        if (threadsAllowedToBlockForConnectionMultiplier != null) {
            builder.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);
        }

        Integer maxWaitTime = state.getProperty(MAX_WAIT_TIME, false, Integer.class);
        if (maxWaitTime != null) {
            builder.maxWaitTime(maxWaitTime);
        }

        Integer connectionTimeout = state.getProperty(CONNECT_TIMEOUT, false, Integer.class);
        if (connectionTimeout != null) {
            builder.connectTimeout(connectionTimeout);
        }

        Boolean socketKeepAlive = state.getProperty(SOCKET_KEEP_ALIVE, false, Boolean.class);
        if (socketKeepAlive != null) {
            builder.socketKeepAlive(socketKeepAlive);
        }

        Boolean autoConnectRetry = state.getProperty(AUTOCONNECT_RETRY, false, Boolean.class);
        if (autoConnectRetry != null) {
            builder.autoConnectRetry(autoConnectRetry);
        }

        Long maxAutoConnectRetryTime = state.getProperty(MAX_AUTOCONNECT_RETRY_TIME, false, Long.class);
        if (maxAutoConnectRetryTime != null) {
                builder.maxAutoConnectRetryTime(maxAutoConnectRetryTime);
        }

        Boolean cursorFinalizerEnabled = state.getProperty(CURSOR_FINALIZER_ENABLED, false, Boolean.class);
        if (cursorFinalizerEnabled != null) {
            builder.cursorFinalizerEnabled(cursorFinalizerEnabled);
        }

        Boolean alwaysUseMBeans = state.getProperty(ALWAYS_USE_MBEANS, false, Boolean.class);
        if (alwaysUseMBeans != null) {
            builder.alwaysUseMBeans(alwaysUseMBeans);
        }

        this.mongoClientOptions = builder.build();
    }

    public MongoClientOptionsState(Resource parent, MongoClientOptions mongoClientOptions) {
        super(parent);
        this.mongoClientOptions = mongoClientOptions;
    }

    public MongoClientOptions getMongoClientOptions() {
        return mongoClientOptions;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map properties = new HashMap<>();
        properties.put(DESCRIPTION, mongoClientOptions.getDescription());
        properties.put(CONNECTIONS_PER_HOST, mongoClientOptions.getConnectionsPerHost());
        properties.put(THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER, mongoClientOptions.getThreadsAllowedToBlockForConnectionMultiplier());
        properties.put(MAX_WAIT_TIME, mongoClientOptions.getMaxWaitTime());
        properties.put(CONNECT_TIMEOUT, mongoClientOptions.getConnectTimeout());
        properties.put(SOCKET_KEEP_ALIVE, mongoClientOptions.isSocketKeepAlive());
        properties.put(AUTOCONNECT_RETRY, mongoClientOptions.isAutoConnectRetry());
        properties.put(MAX_AUTOCONNECT_RETRY_TIME, mongoClientOptions.getMaxAutoConnectRetryTime());
        properties.put(CURSOR_FINALIZER_ENABLED, mongoClientOptions.isCursorFinalizerEnabled());
        properties.put(ALWAYS_USE_MBEANS, mongoClientOptions.isAlwaysUseMBeans());
        return properties;
    }
}
