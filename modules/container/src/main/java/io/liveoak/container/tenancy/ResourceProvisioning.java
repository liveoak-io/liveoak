package io.liveoak.container.tenancy;

import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ResourceProvisioning {

    private final String factoryType;
    private final String resourceType;
    private final ResourceState descriptor;

    public ResourceProvisioning(String factoryType, String resourceType, ResourceState descriptor) {
        this.factoryType = factoryType;
        this.resourceType = resourceType;
        this.descriptor = descriptor;
    }

}
