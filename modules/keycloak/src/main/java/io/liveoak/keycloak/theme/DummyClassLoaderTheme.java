package io.liveoak.keycloak.theme;

import org.keycloak.theme.ClassLoaderTheme;

import java.io.IOException;

/**
 * @author Alexandre Mendonca
 *
 * FIXME: Just so that it picks LO classloader instead of KC's. Remove when not needed.
 *
 */
public class DummyClassLoaderTheme extends ClassLoaderTheme {

    public DummyClassLoaderTheme(String name, Type type) throws IOException {
        super(name, type);
    }
}
