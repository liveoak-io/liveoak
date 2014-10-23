package io.liveoak.container.resource;

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
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MockExtension implements Extension {

    public static ServiceName resource(String appId, String id) {
        return ServiceName.of("mock", "resource", appId, id);
    }

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate(new DefaultRootResource(context.id()));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        String appId = context.application().id();

        MockResource resource = new MockResource(context.resourceId());

        ServiceTarget target = context.target();

        target.addService(resource(appId, context.resourceId()), new ValueService<RootResource>(new ImmediateValue<>(resource)))
                .install();

        context.mountPublic(resource(appId, context.resourceId()));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}