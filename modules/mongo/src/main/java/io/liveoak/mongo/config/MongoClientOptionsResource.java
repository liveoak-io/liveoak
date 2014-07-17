package io.liveoak.mongo.config;

import com.mongodb.MongoClientOptions;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoClientOptionsResource implements Resource {

    RootMongoConfigResource parent;
    MongoClientOptions mongoClientOptions;
    public static final String ID = "MongoClientOptions";

    public enum Options {
        DESCRIPTION("description"),
        CONNECTIONS_PER_HOST("connectionsPerHost"),
        THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER("threadsAllowedToBlockForConnectionMultiplier"),
        MAX_WAIT_TIME("maxWaitTime"),
        CONNECT_TIMEOUT("connectTimeout"),
        SOCKET_KEEP_ALIVE("socketKeepAlive"),
        AUTOCONNECT_RETRY("autoConnectRetry"),
        MAX_AUTOCONNECT_RETRY_TIME("maxAutoConnectRetryTime"),
        CURSOR_FINALIZER_ENABLED("cursorFinalizerEnabled"),
        ALWAYS_USE_MBEANS("alwaysUseMBeans");


        private final String propertyName;

        Options(String propertyName) {
            this.propertyName = propertyName;
        }

        public String toString() {
            return propertyName;
        }

    }


    public MongoClientOptionsResource(RootMongoConfigResource parent, MongoClientOptions mongoClientOptions) {
        this.parent = parent;
        this.mongoClientOptions = mongoClientOptions;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(Options.DESCRIPTION.toString(), mongoClientOptions.getDescription());
        sink.accept(Options.CONNECTIONS_PER_HOST.toString(), mongoClientOptions.getConnectionsPerHost());
        sink.accept(Options.THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER.toString(), mongoClientOptions.getThreadsAllowedToBlockForConnectionMultiplier());
        sink.accept(Options.MAX_WAIT_TIME.toString(), mongoClientOptions.getMaxWaitTime());
        sink.accept(Options.CONNECT_TIMEOUT.toString(), mongoClientOptions.getConnectTimeout());
        sink.accept(Options.SOCKET_KEEP_ALIVE.toString(), mongoClientOptions.isSocketKeepAlive());
        sink.accept(Options.AUTOCONNECT_RETRY.toString(), mongoClientOptions.isAutoConnectRetry());
        sink.accept(Options.MAX_AUTOCONNECT_RETRY_TIME.toString(), mongoClientOptions.getMaxAutoConnectRetryTime());
        sink.accept(Options.CURSOR_FINALIZER_ENABLED.toString(), mongoClientOptions.isCursorFinalizerEnabled());
        sink.accept(Options.ALWAYS_USE_MBEANS.toString(), mongoClientOptions.isAlwaysUseMBeans());

        sink.close();
    }

    public MongoClientOptions updateMongoClientOptions(ResourceState state) throws Exception {
        MongoClientOptions.Builder builder = MongoClientOptions.builder();

        String description = (String) state.getProperty(Options.DESCRIPTION.toString());
        if (description != null) {
            builder.description(description);
        } else if (mongoClientOptions.getDescription() != null) {
            builder.description(mongoClientOptions.getDescription());
        } else {
            builder.description("liveoak"); //TODO: don't hard code this here
        }

        Integer connectionsPerHost = (Integer) state.getProperty(Options.CONNECTIONS_PER_HOST.toString());
        if (connectionsPerHost != null) {
            builder.connectionsPerHost(connectionsPerHost);
        } else {
            builder.connectionsPerHost(mongoClientOptions.getConnectionsPerHost());
        }

        Integer threadsAllowedToBlockForConnectionMultiplier = (Integer) state.getProperty(Options.THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER.toString());
        if (threadsAllowedToBlockForConnectionMultiplier != null) {
            builder.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);
        } else {
            builder.threadsAllowedToBlockForConnectionMultiplier(mongoClientOptions.getThreadsAllowedToBlockForConnectionMultiplier());
        }

        Integer maxWaitTime = (Integer) state.getProperty(Options.MAX_WAIT_TIME.toString());
        if (maxWaitTime != null) {
            builder.maxWaitTime(maxWaitTime);
        } else {
            builder.maxWaitTime(mongoClientOptions.getMaxWaitTime());
        }

        Integer connectionTimeout = (Integer) state.getProperty(Options.CONNECT_TIMEOUT.toString());
        if (connectionTimeout != null) {
            builder.connectTimeout(connectionTimeout);
        } else {
            builder.connectTimeout(mongoClientOptions.getConnectTimeout());
        }

        Boolean socketKeepAlive = (Boolean) state.getProperty(Options.SOCKET_KEEP_ALIVE.toString());
        if (socketKeepAlive != null) {
            builder.socketKeepAlive(socketKeepAlive);
        } else {
            builder.socketKeepAlive(mongoClientOptions.isSocketKeepAlive());
        }

        Boolean autoConnectRetry = (Boolean) state.getProperty(Options.AUTOCONNECT_RETRY.toString());
        if (autoConnectRetry != null) {
            builder.autoConnectRetry(autoConnectRetry);
        } else {
            builder.autoConnectRetry(mongoClientOptions.isAutoConnectRetry());
        }

        Object maxAutoConnectRetryTime = state.getProperty(Options.MAX_AUTOCONNECT_RETRY_TIME.toString());
        if (maxAutoConnectRetryTime != null) {
            if (maxAutoConnectRetryTime instanceof Integer) {
                builder.maxAutoConnectRetryTime((int) maxAutoConnectRetryTime);
            } else {
                builder.maxAutoConnectRetryTime((long) maxAutoConnectRetryTime);
            }
        } else {
            builder.maxAutoConnectRetryTime(mongoClientOptions.getMaxAutoConnectRetryTime());
        }

        Boolean cursorFinalizerEnabled = (Boolean) state.getProperty(Options.CURSOR_FINALIZER_ENABLED.toString());
        if (cursorFinalizerEnabled != null) {
            builder.cursorFinalizerEnabled(cursorFinalizerEnabled);
        } else {
            builder.cursorFinalizerEnabled(mongoClientOptions.isCursorFinalizerEnabled());
        }

        Boolean alwaysUseMBeans = (Boolean) state.getProperty(Options.ALWAYS_USE_MBEANS.toString());
        if (alwaysUseMBeans != null) {
            builder.alwaysUseMBeans(alwaysUseMBeans);
        } else {
            builder.alwaysUseMBeans(mongoClientOptions.isAlwaysUseMBeans());
        }

        this.mongoClientOptions = builder.build();
        return this.mongoClientOptions;
    }

    @Override
    public Resource parent() {
        return null;
    }

    @Override
    public String id() {
        return null;
    }
}
