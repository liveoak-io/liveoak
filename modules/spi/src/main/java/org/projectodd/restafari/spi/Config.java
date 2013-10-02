package org.projectodd.restafari.spi;

import java.util.Set;

//TODO: I think this and ObjectResource can be (or derive from) the same interface based off of Map.
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
