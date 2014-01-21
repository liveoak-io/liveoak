package io.liveoak.container.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 */
public class SystemExtensionContextImpl implements SystemExtensionContext {

    public SystemExtensionContextImpl(ServiceTarget target, String id, ServiceName configurationServiceName) {
        this.target = target;
        this.id = id;
        this.configurationServiceName = configurationServiceName;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public ServiceTarget target() {
        return this.target;
    }

    public ServiceName configurationServiceName() {
        return this.configurationServiceName;
    }

    private ServiceTarget target;
    private String id;
    private final ServiceName configurationServiceName;


}
