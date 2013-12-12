package io.liveoak.container;

import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class LiveOak {
    public static final ServiceName LIVEOAK = ServiceName.of( "liveoak" );
    public static final ServiceName CONTAINER = LIVEOAK.append("container");
    public static final ServiceName SUBSCRIPTION_MANAGER = LIVEOAK.append("subscription-manager");
    public static final ServiceName CODEC_MANAGER = LIVEOAK.append("codec-manager");
    public static final ServiceName PIPELINE_CONFIGURATOR = LIVEOAK.append("pipeline-configurator");
    public static final ServiceName WORKER_POOL = LIVEOAK.append("worker-pool");
    public static final ServiceName DIRECT_CONNECTOR = LIVEOAK.append( "direct-connector" );

    public static final ServiceName DEPLOYER = LIVEOAK.append( "deployer" );
    public static final ServiceName DIRECT_DEPLOYER = DEPLOYER.append( "direct" );
    public static final ServiceName DEPLOYMENT_MANAGER = LIVEOAK.append( "deployment-manager" );

    public static final ServiceName NOTIFIER = LIVEOAK.append( "notifier" );

    public static final ServiceName VERTX = LIVEOAK.append( "vertx" );
    public static final ServiceName VERTX_PLATFORM_MANAGER = VERTX.append( "platform-manager" );

    public static final ServiceName SERVER = LIVEOAK.append( "server" );
    public static final ServiceName CODEC = LIVEOAK.append( "codec" );

    private static final ServiceName RESOURCE = LIVEOAK.append( "resource" );
    private static final ServiceName RESOURCE_FACTORY = LIVEOAK.append( "resource-factory" );

    public static ServiceName server(String name) {
        return SERVER.append( name );
    }

    public static ServiceName codec(String name) {
        return CODEC.append( name );
    }

    public static ServiceName codecInstaller(String name) {
        return CODEC.append( name, "installer" );
    }

    public static ServiceName resource(String id) {
        return RESOURCE.append( id );
    }

    public static ServiceName rootResourceFactory(String type) {
        return RESOURCE_FACTORY.append( type );
    }

}
