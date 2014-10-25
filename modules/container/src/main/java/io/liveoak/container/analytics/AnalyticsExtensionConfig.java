/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.analytics;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class AnalyticsExtensionConfig {

    private boolean enabled = true;
    private boolean appsEnabledByDefault = true;
    private String rhqMetricsUrl = "http://localhost:8080/rhq-metrics/event-log";

    public void enabled(boolean val) {
        enabled = val;
    }

    public boolean enabled() {
        return enabled;
    }

    public void appsEnabledByDefault(boolean val) {
        appsEnabledByDefault = val;
    }

    public boolean appsEnabledByDefault() {
        return appsEnabledByDefault;
    }

    public void rhqMetricsUrl(String val) {
        rhqMetricsUrl = val;
    }

    public String rhqMetricsUrl() {
        return rhqMetricsUrl;
    }
}
