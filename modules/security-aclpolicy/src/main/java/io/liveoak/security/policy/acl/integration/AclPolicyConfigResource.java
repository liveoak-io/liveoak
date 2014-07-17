/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.acl.integration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.common.util.ResourceConversionUtils;
import io.liveoak.security.policy.acl.impl.AclPolicy;
import io.liveoak.security.policy.acl.impl.AclPolicyConfig;
import io.liveoak.security.policy.acl.impl.AclPolicyConfigurator;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyConfigResource implements RootResource, SynchronousResource {

    public static final String AUTO_RULES_PROPERTY = "autoRules";

    private final String id;
    private final AclPolicy aclPolicy;
    private AclPolicyConfig aclPolicyConfig;
    private Resource parent;

    public AclPolicyConfigResource(String id, AclPolicy aclPolicy) {
        this.id = id;
        this.aclPolicy = aclPolicy;
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
        ObjectMapper om = ObjectMapperFactory.create();
        // TODO: performance as Object is converted couple of times into various formats...
        String str = om.writeValueAsString(this.aclPolicyConfig);
        ObjectNode objectNode = om.readValue(str, ObjectNode.class);
        ResourceState resourceState = ConversionUtils.convert(objectNode);
        List<ResourceState> childResourceStates = (List<ResourceState>) resourceState.getProperty(AUTO_RULES_PROPERTY);
        List<Resource> childResources = ResourceConversionUtils.convertList(childResourceStates, this);
        resourceState.putProperty(AUTO_RULES_PROPERTY, childResources);
        return resourceState;
    }

    @Override
    public void properties(ResourceState props) throws Exception {
        // Keep just "autoRules" . Other props not important for us
        Set<String> namesCopy = new HashSet<>(props.getPropertyNames());
        for (String propName : namesCopy) {
            if (!AUTO_RULES_PROPERTY.equals(propName)) {
                props.removeProperty(propName);
            }
        }

        ObjectNode objectNode = ConversionUtils.convert(props);
        ObjectMapper om = ObjectMapperFactory.create();
        this.aclPolicyConfig = om.readValue(objectNode.toString(), AclPolicyConfig.class);

        new AclPolicyConfigurator().configure(this.aclPolicy, this.aclPolicyConfig);
    }
}
