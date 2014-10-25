/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.analytics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class AnalyticsService implements Service<AnalyticsService> {

    private Analytics analytics;

    private Map<String, AnalyticsApplicationConfig> appConfig = new ConcurrentHashMap<>();

    private AnalyticsExtensionConfig globalConfig;

    private static AnalyticsService instance;

    public static AnalyticsService instance() {
        return instance;
    }

    @Override
    public void start(StartContext context) throws StartException {
        analytics = new Analytics();
        analytics.start();
        instance = this;
    }

    @Override
    public void stop(StopContext context) {
        analytics.stop();
        analytics = null;
        instance = null;
    }

    @Override
    public AnalyticsService getValue() {
        return this;
    }

    public void addApplicationConfig(String appId, AnalyticsApplicationConfig config) {
        appConfig.put(appId, config);
    }

    public void removeApplicationConfig(String appId) {
        appConfig.remove(appId);
    }

    public void globalConfig(AnalyticsExtensionConfig config) {
        globalConfig = config;
        analytics.config(config);
    }

    public void event(AnalyticsEvent event) {
        if (analytics != null && globalConfig != null && globalConfig.enabled()) {

            String app = event.getApplication();
            AnalyticsApplicationConfig appConf = app != null ? appConfig.get(event.getApplication()) : null;

            boolean appEnabled = (appConf == null && globalConfig.appsEnabledByDefault())
                    || (appConf != null && appConf.enabled());

            if (appEnabled) {
                analytics.event(event);
            }
        }
    }

    public boolean enabled() {
        return globalConfig != null && globalConfig.enabled();
    }
}