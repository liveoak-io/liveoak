package io.liveoak.keycloak.theme;

import org.keycloak.freemarker.Theme;
import org.keycloak.freemarker.ThemeProvider;
import org.keycloak.theme.ClassLoaderTheme;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexandre Mendonca
 */
public class LiveOakLoginThemeProvider implements ThemeProvider {

    public static final String LIVEOAK = "liveoak";

    private static Set<String> defaultThemes = new HashSet<String>();

    static {
        defaultThemes.add(LIVEOAK);
    }

    @Override
    public int getProviderPriority() {
        return 0;
    }

    @Override
    public Theme createTheme(String name, Theme.Type type) throws IOException {
        if (hasTheme(name, type)) {
            return new ClassLoaderTheme(name, type, getClass().getClassLoader());
        } else {
            return null;
        }
    }

    @Override
    public Set<String> nameSet(Theme.Type type) {
        if (type == Theme.Type.LOGIN /* || type == Theme.Type.ACCOUNT */) {
            return defaultThemes;
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public boolean hasTheme(String name, Theme.Type type) {
        return nameSet(type).contains(name);
    }

}
