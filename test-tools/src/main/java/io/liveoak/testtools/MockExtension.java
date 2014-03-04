package io.liveoak.testtools;

import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.DefaultRootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

import java.lang.reflect.Constructor;

/**
 * @author Bob McWhirter
 */
public class MockExtension implements Extension {

    private final Class<? extends RootResource> resourceClass;

    public static ServiceName resource(String appId, Class<? extends RootResource> cls) {
        return LiveOak.resource( appId, cls.getName() );
    }

    public MockExtension(Class<? extends RootResource> resourceClass) {
        this.resourceClass = resourceClass;
    }

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate( new DefaultRootResource( context.id() ));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        ServiceTarget target = context.target();

        Constructor<? extends RootResource> ctor = this.resourceClass.getConstructor(String.class);

        RootResource resource = ctor.newInstance(context.resourceId());
        ServiceName name = LiveOak.resource(context.application().id(), context.resourceId());

        target.addService(name, new ValueService<RootResource>(new ImmediateValue<>(resource)))
                .install();

        context.mountPublic(name);

        context.mountPrivate( new DefaultRootResource( context.resourceId()));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
