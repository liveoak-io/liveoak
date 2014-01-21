package io.liveoak.container.extension.reconfig;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class ReconfigurableExtension implements Extension {

    public static ServiceName mainClient(String id) {
        return ServiceName.of( "reconfig", "client", id );
    }

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        ServiceTarget target = context.target();

        ClientService client = new ClientService();
        target.addService( mainClient( context.id() ), client )
                .addDependency( context.configurationServiceName(), ObjectNode.class, client.configInjector() )
                .install();
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
