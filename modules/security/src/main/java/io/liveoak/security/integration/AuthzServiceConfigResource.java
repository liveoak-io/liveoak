/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.integration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.common.util.ResourceConversionUtils;
import io.liveoak.security.spi.AuthzServiceConfig;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzServiceConfigResource implements RootResource, SynchronousResource {

    public static final String POLICIES_PROPERTY = "policies";

    private final String id;
    private final AuthzServiceRootResource authzRootResource;
    private Resource parent;

    public AuthzServiceConfigResource(String id, AuthzServiceRootResource authzRootResource) {
        this.id = id;
        this.authzRootResource = authzRootResource;
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
        String str = om.writeValueAsString(this.authzRootResource.getConfig());
        ObjectNode objectNode = om.readValue(str, ObjectNode.class);
        ResourceState resourceState = ConversionUtils.convert(objectNode);
        List<ResourceState> childResourceStates = (List<ResourceState>) resourceState.getProperty(POLICIES_PROPERTY);
        if (childResourceStates != null) {
            List<Resource> childResources = ResourceConversionUtils.convertList(childResourceStates, this);
            resourceState.putProperty(POLICIES_PROPERTY, childResources);
        }
        return resourceState;
    }

    @Override
    public void properties(ResourceState props) throws Exception {
        // Keep just "policies" . Other props not important for us
        Set<String> namesCopy = new HashSet<>(props.getPropertyNames());
        for (String propName : namesCopy) {
            if (!POLICIES_PROPERTY.equals(propName)) {
                props.removeProperty(propName);
            }
        }

        ObjectNode objectNode = ConversionUtils.convert(props);
        ObjectMapper om = ObjectMapperFactory.create();
        AuthzServiceConfig authzConfig = om.readValue(objectNode.toString(), AuthzServiceConfig.class);

        this.authzRootResource.setConfig(authzConfig);
    }
}
