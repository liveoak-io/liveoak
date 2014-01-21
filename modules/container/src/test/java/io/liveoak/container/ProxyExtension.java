package io.liveoak.container;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 */
public class ProxyExtension implements Extension {

    public static ServiceName resource(String orgId, String appId, String id) {
        return ServiceName.of( "proxy", "resource", orgId, appId, id );
    }

    @Override
    public void extend(SystemExtensionContext context) throws Exception {

    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        String orgId = context.application().organization().id();
        String appId = context.application().id();

        ServiceTarget target = context.target();

        ProxyResourceService proxy = new ProxyResourceService( context.id() );

        target.addService( resource( orgId, appId, context.id() ),  proxy )
                .addDependency(LiveOak.CLIENT, Client.class, proxy.clientInjector() )
                .addDependency( context.configurationServiceName(), ObjectNode.class, proxy.configurationInjector() )
                .install();

        context.mountPublic( resource( orgId, appId, context.id() ));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
