package io.liveoak.container.extension;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.liveoak.common.DefaultMountPointResource;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.container.extension.ExtensionConfigurationManager;
import io.liveoak.container.extension.SystemExtensionContextImpl;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ModuleResourceRegistry extends DefaultMountPointResource {

    Extension extension;
    ServiceTarget target;
    ExtensionConfigurationManager configurationManager;

    public ModuleResourceRegistry(String id, Extension extension, ServiceTarget target, ExtensionConfigurationManager configurationManager) {
        super(id);
        this.extension = extension;
        this.target = target;
        this.configurationManager = configurationManager;
    }

    @Override
    public void registerResource(Resource resource) {
        super.registerResource(resource);
        if (resourceListeners.get(resource.id()) != null) {
            resourceListeners.get(resource.id()).ResourceRegistered(resource);
        }
    }

    @Override
    public void unregisterResource(Resource resource) {
        super.unregisterResource(resource);
        if (resourceListeners.get(resource.id()) != null) {
            resourceListeners.get(resource.id()).ResourceUnRegistered(resource);
        }
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        try {
            if (state.id() == null) {
                String id = state.getProperty("id", false, String.class);
                if (id != null) {
                    state.id(id);
                } else {
                    state.id(UUID.randomUUID().toString());
                }
            }

            if (member(new RequestContext.Builder().build(), state.id()) != null) {
                responder.resourceAlreadyExists(state.id());
                return;
            }

            resourceListeners.put(state.id(), new ResourceListener() {
                @Override
                public void ResourceRegistered(Resource resource) {
                    responder.resourceUpdated(resource);
                }
            });

            ServiceName moduleName = Services.resource(ZeroExtension.APPLICATION_ID, "system").append("module").append(id());
            SystemExtensionContext extInstanceContext = new SystemExtensionContextImpl(target, id(), state.id(), moduleName, ConversionUtils.convert(state), this);
            extension.instance(state.id(), extInstanceContext);

            if (configurationManager != null) {
                configurationManager.createResource(state.id(), id(), ConversionUtils.convert(state));
            }

        } catch (Exception e) {
            responder.internalError(e.getMessage(), e);
        }
    }

    @Override
    public void deleteMember(RequestContext ctx, String id, Responder responder) {

        if (id.equals("module")) {
            responder.deleteNotSupported(registry.get(id));
            return;
        }

        Resource resource = registry.get(id);
        if (resource == null) {
            responder.noSuchResource(id);
        } else {
            registry.remove(id);

            if (configurationManager != null) {
                try {
                    configurationManager.removeResource(id, id());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ServiceController serviceController = serviceControllers.get(id);
            if (serviceController != null && serviceController.getServiceContainer() != null) {

                //TODO: figure out a better way to handle this......
                ServiceContainer serviceContainer = serviceController.getServiceContainer();

                ServiceName service = Services.instanceResource(id(), id);

                if (serviceContainer.getService(service) != null) {
                    serviceContainer.getService(Services.instanceResource(id(), id)).setMode(ServiceController.Mode.REMOVE);
                    serviceContainer.getService(Services.instanceResource(id(), id).append("wrapper")).setMode(ServiceController.Mode.REMOVE);
                    serviceContainer.getService(Services.instanceResource(id(), id).append("apply-config")).setMode(ServiceController.Mode.REMOVE);
                    serviceContainer.getService(Services.instanceResource(id(), id).append("lifecycle")).setMode(ServiceController.Mode.REMOVE);
                }

                serviceController.setMode(ServiceController.Mode.REMOVE);
                serviceControllers.remove(id);
            }

            responder.resourceDeleted(resource);
        }
    }

    public void addServiceController(String id, ServiceController serviceController) {
        serviceControllers.put(id, serviceController);
    }

    private Map<String, ResourceListener> resourceListeners = new ConcurrentHashMap<>();
    private Map<String, ServiceController> serviceControllers = new ConcurrentHashMap<>();

    private interface ResourceListener {
        default void ResourceRegistered(Resource resource) {}
        default void ResourceUnRegistered(Resource resource) {}
    }
}
