package io.liveoak.container;

import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class InMemoryDBExtension implements Extension{

    public static ServiceName resource(String orgId, String appId, String id) {
        return ServiceName.of("in-memory", "resource", orgId, appId, id);
    }

    @Override
    public void extend(SystemExtensionContext context) throws Exception {

    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        String orgId = context.application().organization().id();
        String appId = context.application().id();

        InMemoryDBResource resource = new InMemoryDBResource( context.id() );

        ServiceTarget target = context.target();

        target.addService( resource( orgId, appId, context.id() ), new ValueService<RootResource>( new ImmediateValue<>( resource ) ) )
                .install();

        context.mountPublic( resource( orgId, appId, context.id() ) );
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
