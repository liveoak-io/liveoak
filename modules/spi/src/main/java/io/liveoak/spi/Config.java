/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

import java.util.Set;

//TODO: I think this and ObjectResourceState can be (or derive from) the same interface based off of Map.
public interface Config {

    <T> T put(String name, T value);

    <T> T get(String name, T defaultValue) throws ConfigurationException;

    default <T> T getRequired(String name) throws ConfigurationException {
        T t = get(name, null);
        if (t == null) {
            throw new ConfigurationException(name + " is required.");
        }

        return t;
    }

    Set<String> names();
}
