package io.liveoak.container.tenancy;

import java.util.concurrent.ConcurrentHashMap;

import java.util.Map;

import io.liveoak.common.DefaultMountPointResource;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.container.extension.SystemExtensionContextImpl;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class InstancesResourceRegistry extends DefaultMountPointResource {

    Extension extension;
    ServiceTarget target;

    public InstancesResourceRegistry(Resource parent, String id) {
        super(parent, id);
    }

    public InstancesResourceRegistry(String id, Extension extension, ServiceTarget target) {
        super(id);
        this.extension = extension;
        this.target = target;
    }

    @Override
    public void registerResource(Resource resource) {
        super.registerResource(resource);
        if (resourceListeners.get(resource.id()) != null) {
            resourceListeners.get(resource.id()).ResourceRegistered(resource);
        }
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        try {
            SystemExtensionContext extInstanceContext = new SystemExtensionContextImpl(target, id(), Services.systemResource(id()), ConversionUtils.convert(state));
            extension.instance(state.id(), extInstanceContext);

            resourceListeners.put(state.id(), new ResourceListener() {
                @Override
                public void ResourceRegistered(Resource resource) {
                    responder.resourceUpdated(resource);
                }
            });

        } catch (Exception e) {
            responder.internalError(e.getMessage(), e);
        }
    }

    private Map<String, ResourceListener> resourceListeners = new ConcurrentHashMap<>();

    private interface ResourceListener {
        void ResourceRegistered(Resource resource);
    }
}
