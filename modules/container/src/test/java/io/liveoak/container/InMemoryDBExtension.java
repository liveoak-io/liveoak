package io.liveoak.container;

import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.DefaultRootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class InMemoryDBExtension implements Extension {

    public static ServiceName resource(String appId, String id) {
        return ServiceName.of("in-memory", "resource", appId, id);
    }

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate(new DefaultRootResource(context.id()));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        String appId = context.application().id();

        InMemoryDBResource resource = new InMemoryDBResource(context.resourceId());

        ServiceTarget target = context.target();

        target.addService(resource(appId, context.resourceId()), new ValueService<RootResource>(new ImmediateValue<>(resource)))
                .install();

        context.mountPublic(resource(appId, context.resourceId()));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
