/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.drools.integration;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.common.util.ResourceConversionUtils;
import io.liveoak.security.policy.drools.impl.DroolsPolicy;
import io.liveoak.security.policy.drools.impl.DroolsPolicyConfigurator;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DroolsPolicyConfigResource implements RootResource, SynchronousResource {

    private final String id;
    private final DroolsPolicy droolsPolicy;
    private DroolsPolicyConfig uriPolicyConfig;
    private Resource parent;

    public DroolsPolicyConfigResource(String id, DroolsPolicy droolsPolicy) {
        this.id = id;
        this.droolsPolicy = droolsPolicy;
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
        this.uriPolicyConfig = om.readValue(objectNode.toString(), DroolsPolicyConfig.class);

        new DroolsPolicyConfigurator().configure(this.droolsPolicy, this.uriPolicyConfig);
    }
}
