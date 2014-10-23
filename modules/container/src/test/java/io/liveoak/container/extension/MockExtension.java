package io.liveoak.container.extension;

import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class MockExtension implements Extension {

    public MockExtension() {
    }

    public MockExtension(String id) {
        this.id = id;
    }

    public static ServiceName resource(String id) {
        return ServiceName.of("mock", "resource", id);
    }

    public static ServiceName adminResource(String id) {
        return ServiceName.of("mock", "admin-resource", id);
    }

    public static ServiceName adminResource(String appId, String id) {
        return ServiceName.of("mock", "app-admin-resource", appId, id);
    }

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        MockAdminResource admin = new MockAdminResource(context.id(), "system");
        ServiceName serviceName = this.id != null ? adminResource(context.id()).append(this.id) : adminResource(context.id());
        context.target().addService(serviceName, new ValueService<>(new ImmediateValue<>(admin)))
                .install();

        context.mountPrivate(serviceName);
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        MockAdminResource admin = new MockAdminResource(context.resourceId(), "application");
        context.mountPrivate(admin);

        MockResource resource = new MockResource(context.resourceId());
        context.mountPublic(resource);
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }

    @Override
    public void instance(String id, SystemExtensionContext context) throws Exception {
        MockAdminResource instance = new MockAdminResource(context.id(), "instance");
        context.target().addService(adminResource(context.id()), new ValueService<>(new ImmediateValue<>(instance)))
                .install();

        context.mountInstance(adminResource(context.id()));
    }

    private String id = null;
}
