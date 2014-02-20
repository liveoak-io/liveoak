package io.liveoak.container.extension;

import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class MockExtension implements Extension {

    public static ServiceName resource(String id) {
        return ServiceName.of( "mock", "resource", id );
    }

    public static ServiceName adminResource(String id) {
        return ServiceName.of( "mock", "admin-resource", id );
    }

    public static ServiceName adminResource(String appId, String id) {
        return ServiceName.of( "mock", "app-admin-resource", appId, id );
    }

    @Override
    public void extend(SystemExtensionContext context) throws Exception {

        MockAdminResource admin = new MockAdminResource( context.id(), "system" );
        context.target().addService(adminResource( context.id() ), new ValueService<MockAdminResource>(new ImmediateValue<>(admin)))
                .install();

        context.mountPrivate( adminResource( context.id() ));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        MockAdminResource admin = new MockAdminResource( context.resourceId(), "application" );
        context.target().addService(adminResource( context.application().id(), context.resourceId() ), new ValueService<MockAdminResource>(new ImmediateValue<>(admin)))
                .install();
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
