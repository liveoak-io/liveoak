package io.liveoak.redirect.https;

import io.liveoak.spi.Services;
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

    final RootResource rootResource;

    public MockExtension(RootResource rootResource) {
        this.rootResource = rootResource;
    }

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate( new DefaultRootResource( context.id() ));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        ServiceTarget target = context.target();

        ServiceName name = Services.resource(context.application().id(), context.resourceId());

        target.addService(name, new ValueService<RootResource>(new ImmediateValue<>(rootResource)))
                .install();

        context.mountPublic(name);

        context.mountPrivate( new DefaultRootResource( context.resourceId()));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
        //do nothing for now
    }
}
