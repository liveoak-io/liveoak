package io.liveoak.container;

import io.liveoak.spi.LiveOak;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class ProxyExtension implements Extension {

    public static ServiceName resource(String appId, String id) {
        return ServiceName.of("proxy", "resource", appId, id);
    }

    public static ServiceName adminResource(String appId, String id) {
        return ServiceName.of("proxy", "admin-resource", appId, id);
    }

    @Override
    public void extend(SystemExtensionContext context) throws Exception {

    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        String appId = context.application().id();

        ServiceTarget target = context.target();

        ServiceName configName = adminResource(appId, context.resourceId() ).append( "config" );

        ProxyResourceService proxy = new ProxyResourceService(context.resourceId());

        target.addService(resource(appId, context.resourceId()), proxy)
                .addDependency(LiveOak.CLIENT, Client.class, proxy.clientInjector())
                .addDependency(configName, ProxyConfig.class, proxy.configurationInjector())
                .install();

        context.mountPublic(resource(appId, context.resourceId()));

        ValueInjectionService<ProxyConfig> config = new ValueInjectionService<>();

        ServiceController<ProxyConfig> configController = target.addService(configName, config )
                .addDependency( adminResource( appId, context.resourceId() ), ProxyConfig.class, config.getInjector() )
                .install();

        ProxyAdminResource admin = new ProxyAdminResource(context.resourceId(), configController);
        target.addService(adminResource(appId, context.resourceId()), new ValueService<ProxyAdminResource>(new ImmediateValue<>(admin)))
                .install();

        context.mountPrivate(adminResource(appId, context.resourceId()));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
