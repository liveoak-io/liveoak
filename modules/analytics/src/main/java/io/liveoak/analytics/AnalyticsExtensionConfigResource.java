/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.analytics;

import java.util.HashMap;
import java.util.Map;

import io.liveoak.container.analytics.AnalyticsExtensionConfig;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
* @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
*/
public class AnalyticsExtensionConfigResource implements RootResource, SynchronousResource {

    private Resource parent;
    private String id;

    private AnalyticsExtensionConfig config = new AnalyticsExtensionConfig();

    public AnalyticsExtensionConfigResource(String id) {
        this.id = id;
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
        HashMap props = new HashMap();
        props.put("enabled", config.enabled());
        props.put("apps-enabled-by-default", config.appsEnabledByDefault());
        props.put("rhq-metrics-url", config.rhqMetricsUrl());
        return props;
    }

    @Override
    public void properties(RequestContext ctx, ResourceState props) throws Exception {
        Boolean val = props.getPropertyAsBoolean("enabled");
        if (val != null) {
            config.enabled(val);
        }

        val = props.getPropertyAsBoolean("apps-enabled-by-default");
        if (val != null) {
            config.appsEnabledByDefault(val);
        }

        String sval = props.getPropertyAsString("rhq-metrics-url");
        if (sval != null) {
            config.rhqMetricsUrl(sval);
        }
    }

    public AnalyticsExtensionConfig config() {
        return config;
    }
}
