package io.liveoak.keycloak.theme;

import org.keycloak.Config;
import org.keycloak.freemarker.ThemeProvider;
import org.keycloak.freemarker.ThemeProviderFactory;
import org.keycloak.provider.ProviderSession;
import org.keycloak.theme.DefaultKeycloakThemeProvider;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LiveOakLoginThemeProviderFactory implements ThemeProviderFactory {

    private LiveOakLoginThemeProvider themeProvider;
    public static final String ID = "liveoak";

    @Override
    public ThemeProvider create(ProviderSession providerSession) {
        return themeProvider;
    }

    @Override
    public void init(Config.Scope config) {
        themeProvider = new LiveOakLoginThemeProvider();
    }

    @Override
    public void close() {
        themeProvider = null;
    }

    @Override
    public String getId() {
        return ID;
    }
}
