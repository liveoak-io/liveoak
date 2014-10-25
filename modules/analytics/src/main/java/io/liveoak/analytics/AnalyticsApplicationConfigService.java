/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.analytics;

import io.liveoak.container.analytics.AnalyticsService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class AnalyticsApplicationConfigService implements Service<AnalyticsApplicationConfigResource> {

    private String id;
    private AnalyticsApplicationConfigResource rootResource;
    private InjectedValue<AnalyticsService> analyticsServiceInjector = new InjectedValue<>();

    public AnalyticsApplicationConfigService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        rootResource = new AnalyticsApplicationConfigResource(id);
        analyticsServiceInjector().getValue().addApplicationConfig(id, rootResource.config());
    }

    @Override
    public void stop(StopContext context) {
        rootResource = null;
        analyticsServiceInjector().getValue().removeApplicationConfig(id);
    }

    @Override
    public AnalyticsApplicationConfigResource getValue() throws IllegalStateException, IllegalArgumentException {
        return rootResource;
    }

    public InjectedValue<AnalyticsService> analyticsServiceInjector() {
        return analyticsServiceInjector;
    }
}
