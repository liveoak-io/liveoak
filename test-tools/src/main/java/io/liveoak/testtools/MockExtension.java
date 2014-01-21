package io.liveoak.testtools;

import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

import java.lang.reflect.Constructor;

/**
 * @author Bob McWhirter
 */
public class MockExtension implements Extension {

    public static ServiceName resource(String orgId, String appId, Class<? extends RootResource> resourceClass) {
        return ServiceName.of( "mock-ext", orgId, appId, resourceClass.getName().toString(), "resource" );
    }

    private final Class<? extends RootResource> resourceClass;

    public MockExtension(Class<? extends RootResource> resourceClass) {
        this.resourceClass = resourceClass;
    }

    @Override
    public void extend(SystemExtensionContext context) throws Exception {

    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        ServiceTarget target = context.target();

        Constructor<? extends RootResource> ctor = this.resourceClass.getConstructor(String.class);

        RootResource resource = ctor.newInstance(context.id());
        ServiceName name = resource( context.application().organization().id(), context.application().id(), this.resourceClass );

        target.addService(name, new ValueService<RootResource>(new ImmediateValue<>(resource)))
                .install();

        context.mountPublic(name);
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
