/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Bob McWhirter
 */
public class PropertiesResource implements SynchronousResource {

    public PropertiesResource(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, String> result = new HashMap<>();
        Properties allProps = System.getProperties();
        for (String key : allProps.stringPropertyNames()) {
            result.put(key, allProps.getProperty(key));
        }
        return result;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return "properties";
    }

    private Resource parent;
}
