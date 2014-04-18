package io.liveoak.security.policy.uri.integration;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.common.util.ResourceConversionUtils;
import io.liveoak.security.policy.uri.impl.URIPolicy;
import io.liveoak.security.policy.uri.impl.URIPolicyConfigurator;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyConfigResource implements RootResource, SynchronousResource {

    private final String id;
    private final URIPolicy uriPolicy;
    private URIPolicyConfig uriPolicyConfig;
    private Resource parent;

    public URIPolicyConfigResource(String id, URIPolicy uriPolicy) {
        this.id = id;
        this.uriPolicy = uriPolicy;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public ResourceState properties() throws Exception {
        ObjectMapper om = new ObjectMapper();
        // TODO: performance as Object is converted couple of times into various formats...
        String str = om.writeValueAsString(this.uriPolicyConfig);
        ObjectNode objectNode = om.readValue(str, ObjectNode.class);
        ResourceState resourceState = ConversionUtils.convert(objectNode);
        List<ResourceState> childResourceStates = (List<ResourceState>)resourceState.getProperty("rules");
        List<Resource> childResources = ResourceConversionUtils.convertList(childResourceStates, this);
        resourceState.putProperty("rules", childResources);
        return resourceState;
    }

    @Override
    public void properties(ResourceState props) throws Exception {
        ObjectNode objectNode = ConversionUtils.convert(props);
        ObjectMapper om = new ObjectMapper();
        this.uriPolicyConfig = om.readValue(objectNode.toString(), URIPolicyConfig.class);

        new URIPolicyConfigurator().configure(uriPolicy, uriPolicyConfig);
    }
}
