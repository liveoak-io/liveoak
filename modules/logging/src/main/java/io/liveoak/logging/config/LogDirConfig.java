/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.logging.config;

import java.util.HashMap;
import java.util.Map;

import io.liveoak.spi.ConfigurationException;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class LogDirConfig {

    // Config fields
    private static final String PATH = "path";
    private static final String FILTER = "filter";

    private final String path;
    private final String filter;

    public LogDirConfig(ResourceState state) throws ConfigurationException {
        path = (String) state.getProperty(PATH);
        if (path == null) {
            throw new ConfigurationException(PATH + " is a required configuration field.");
        }
        filter = (String) state.getProperty(FILTER);
    }

    public String path() {
        return path;
    }

    public String filter() {
        return filter;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>(2);
        map.put(PATH, path);
        map.put(FILTER, filter);

        return map;
    }
}
