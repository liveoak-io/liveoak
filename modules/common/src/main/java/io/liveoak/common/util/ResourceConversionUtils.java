package io.liveoak.common.util;

import java.util.ArrayList;
import java.util.List;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * Return converted objects to the form, which can be added to PropertySink (for example in Resource.readProperties())
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ResourceConversionUtils {

    /**
     * Convert resourceState to resource, which can be safely added to PropertySink
     * @param resourceState with all the properties to be primitives or list of primitives
     * @param parent
     * @return resource
     */
    public static Resource convertResourceState(ResourceState resourceState, Resource parent) {
        return new SynchronousResource() {

            @Override
            public Resource parent() {
                return parent;
            }

            @Override
            public String id() {
                return resourceState.id();
            }

            @Override
            public ResourceState properties(RequestContext ctx) throws Exception {
                return resourceState;
            }
        };
    }

    /**
     * Convert list of resourceStates to list of resources, which can be safely added to PropertySink
     *
     * @param resourceStates where all the ResourceState in it must have either primitive properties or list of primitives
     * @param parent
     * @return list of resources
     */
    public static List<Resource> convertList(List<ResourceState> resourceStates, Resource parent) {
        List<Resource> resources = new ArrayList<>();
        for (ResourceState rs : resourceStates) {
            resources.add(convertResourceState(rs, parent));
        }
        return resources;
    }
}
