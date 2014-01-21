package io.liveoak.spi;

import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class LiveOak {
    public static final ServiceName LIVEOAK = ServiceName.of("liveoak");
    public static final ServiceName SUBSCRIPTION_MANAGER = LIVEOAK.append("subscription-manager");
    public static final ServiceName INTERCEPTOR_MANAGER = LIVEOAK.append("interceptor-manager");
    public static final ServiceName CODEC_MANAGER = LIVEOAK.append("codec-manager");
    public static final ServiceName PIPELINE_CONFIGURATOR = LIVEOAK.append("pipeline-configurator");
    public static final ServiceName WORKER_POOL = LIVEOAK.append("worker-pool");

    public static final ServiceName DEPLOYER = LIVEOAK.append("deployer");
    public static final ServiceName DIRECT_DEPLOYER = DEPLOYER.append("direct");


    public static final ServiceName NOTIFIER = LIVEOAK.append("notifier");

    public static final ServiceName VERTX = LIVEOAK.append("vertx");
    public static final ServiceName VERTX_PLATFORM_MANAGER = VERTX.append("platform-manager");

    private static final ServiceName SERVER = LIVEOAK.append("server");
    public static final ServiceName NETWORK_SERVER = SERVER.append("network");
    public static final ServiceName LOCAL_SERVER = SERVER.append("local");

    public static final ServiceName CLIENT = LIVEOAK.append("client");

    public static final ServiceName CODEC = LIVEOAK.append("codec");

    private static final ServiceName RESOURCE = LIVEOAK.append("resource");
    private static final ServiceName RESOURCE_FACTORY = LIVEOAK.append("resource-extension");
    private static final ServiceName INTERCEPTOR = LIVEOAK.append("interceptor");

    private static final ServiceName GLOBAL = LIVEOAK.append("global");
    public static final ServiceName ORGANIZATION_REGISTRY = GLOBAL.append("registry");

    public static final ServiceName GLOBAL_CONTEXT = GLOBAL.append("context");
    private static final ServiceName ORGANIZATION = LIVEOAK.append("organization");
    private static final ServiceName APPLICATION = LIVEOAK.append("application");

    public static final ServiceName SERVICE_REGISTRY = LIVEOAK.append( "msc", "service-registry" );
    public static final ServiceName SERVICE_CONTAINER = LIVEOAK.append( "msc", "service-container" );
    public static final ServiceName EXTENSION_LOADER = LIVEOAK.append( "extension-loader" );
    public static final ServiceName EXTENSION_INSTALLER = LIVEOAK.append( "extension-installer" );
    public static final ServiceName COMMON_EXTENSIONS = LIVEOAK.append( "common-extensions" );

    public static final ServiceName APPLICATIONS_DIR = LIVEOAK.append( "dir", "applications" );

    public static ServiceName organization(String orgId) {
        return LIVEOAK.append( "organization", orgId );
    }

    public static ServiceName organizationContext(String orgId) {
        return LIVEOAK.append( "context", "organization", orgId );
    }

    public static ServiceName organizationAdminResource(String orgId) {
        return LIVEOAK.append( "admin", "organization", orgId );
    }

    public static ServiceName application(String orgId, String appId) {
        return LIVEOAK.append( "application", orgId, appId );
    }

    public static ServiceName applicationContext(String orgId, String appId) {
        return LIVEOAK.append( "context", "application", orgId, appId );
    }

    public static ServiceName applicationAdminResource(String orgId, String appId) {
        return LIVEOAK.append( "admin", "application", orgId, appId );
    }

    public static ServiceName applicationResource(String orgId, String appId, String resourceId) {
        return LIVEOAK.append( "resource", orgId, appId, resourceId );
    }

    public static ServiceName extension(String id) {
        return LIVEOAK.append( "extension", id );
    }

    public static ServiceName applicationExtension(String orgId, String appId, String extensionId) {
        return LIVEOAK.append( "extension", extensionId, orgId, appId );
    }

    public static ServiceName applicationExtensionAdminResource(String orgId, String appId, String resourceId) {
        return LIVEOAK.append( "resource", "admin", orgId, appId, resourceId );
    }




    public static ServiceName resource(String orgId, String appId, String resourceId) {
        return RESOURCE.append(orgId, appId, resourceId);
    }

    public static ServiceName server(String name, boolean network) {
        if (network) {
            return NETWORK_SERVER.append(name);
        }
        return LOCAL_SERVER.append(name);
    }

    public static ServiceName codec(String name) {
        return CODEC.append(name);
    }

    public static ServiceName codecInstaller(String name) {
        return CODEC.append(name, "installer");
    }

    public static ServiceName resourceFactory(String type) {
        return RESOURCE_FACTORY.append(type);
    }

    public static ServiceName interceptor(String name) {
        return INTERCEPTOR.append(name);
    }

}
