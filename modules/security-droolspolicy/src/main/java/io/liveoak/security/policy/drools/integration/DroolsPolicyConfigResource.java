/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.drools.integration;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.common.util.ResourceConversionUtils;
import io.liveoak.security.policy.drools.impl.DroolsPolicy;
import io.liveoak.security.policy.drools.impl.DroolsPolicyConfigurator;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DroolsPolicyConfigResource implements RootResource, SynchronousResource {

    public static final String RULES_PROPERTY = "rules";

    private final String id;
    private final DroolsPolicy droolsPolicy;
    private DroolsPolicyConfig droolsPolicyConfig;
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
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        ObjectMapper om = ObjectMapperFactory.create();
        // TODO: performance as Object is converted couple of times into various formats...
        String str = om.writeValueAsString(this.droolsPolicyConfig);
        ObjectNode objectNode = om.readValue(str, ObjectNode.class);
        ResourceState resourceState = ConversionUtils.convert(objectNode);
        List<ResourceState> childResourceStates = (List<ResourceState>) resourceState.getProperty(RULES_PROPERTY);
        List<Resource> childResources = ResourceConversionUtils.convertList(childResourceStates, this);
        resourceState.putProperty(RULES_PROPERTY, childResources);
        return new DefaultResourceState(resourceState).propertyMap();
    }

    @Override
    public void properties(ResourceState props) throws Exception {
        // Keep just "rules" . Other props not important for us
        Set<String> namesCopy = new HashSet<>(props.getPropertyNames());
        for (String propName : namesCopy) {
            if (!RULES_PROPERTY.equals(propName)) {
                props.removeProperty(propName);
            }
        }

        ObjectNode objectNode = ConversionUtils.convert(props);
        ObjectMapper om = ObjectMapperFactory.create();
        this.droolsPolicyConfig = om.readValue(objectNode.toString(), DroolsPolicyConfig.class);

        new DroolsPolicyConfigurator().configure(this.droolsPolicy, this.droolsPolicyConfig);
    }
}
