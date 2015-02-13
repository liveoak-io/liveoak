package io.liveoak.applications.templates;

import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class TemplateExtension implements Extension {

    public static final ServiceName SYSTEM_APPLICATION_TEMPLATE_REGISTRY = Services.LIVEOAK.append("system", "application", "templates", "registry");


    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        ServiceName serviceName = Services.systemResource(context.moduleId(), context.id());

        ApplicationTemplateResource applicationTemplateResource = new ApplicationTemplateResource(context.id());
        ValueService applicationTemplateResourceService = new ValueService(new ImmediateValue<>(applicationTemplateResource));
        context.target().addService(serviceName, applicationTemplateResourceService)
                .install();

        context.mountPrivate(serviceName);

        TemplateRegistryService templateRegistryService = new TemplateRegistryService();
        context.target().addService(SYSTEM_APPLICATION_TEMPLATE_REGISTRY, templateRegistryService)
                .addDependency(serviceName, ApplicationTemplateResource.class, templateRegistryService.applicationTemplateResourceInjector())
                .addDependency(serviceName.append("mount"))
                .install();
    }

    @Override
    public void instance(String id, SystemExtensionContext context) throws Exception {
        ServiceName serviceName = Services.instanceResource(context.moduleId(), id);

        ApplicationTemplateService templateService = new ApplicationTemplateService(id);

        context.target().addService(serviceName, templateService)
                .addDependency(SYSTEM_APPLICATION_TEMPLATE_REGISTRY, TemplateRegistry.class, templateService.templateRegistryInjector)
                .install();
        context.mountInstance(serviceName);
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        // Only system level support
        // TODO: throw an error message here if an application tries to use this, right now we just ignore
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
        // Only system level support
        // TODO: throw an error message here if an application tries to use this, right now we just ignore
    }
}
