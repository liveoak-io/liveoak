package io.liveoak.container.extension;

import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Ken Finnigan
 */
public class FilterExtension implements Extension {
    public static ServiceName adminResource(String id) {
        return ServiceName.of("filter", "admin-resource", id);
    }

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        MockAdminResource admin = new MockAdminResource(context.id(), "system");
        context.target().addService(adminResource(context.id()), new ValueService<MockAdminResource>(new ImmediateValue<>(admin)))
                .install();

        context.mountPrivate(adminResource(context.id()));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        MockAdminResource admin = new MockAdminResource(context.resourceId(), "application");
        context.mountPrivate(admin);
        context.mountPublic(admin);
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
    }
}
