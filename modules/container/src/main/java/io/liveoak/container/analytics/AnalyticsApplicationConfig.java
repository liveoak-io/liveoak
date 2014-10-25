/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.analytics;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class AnalyticsApplicationConfig {

    private boolean enabled = true;

    public void enabled(boolean val) {
        enabled = val;
    }

    public boolean enabled() {
        return enabled;
    }
}
