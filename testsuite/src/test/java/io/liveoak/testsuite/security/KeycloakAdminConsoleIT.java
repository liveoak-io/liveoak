package io.liveoak.testsuite.security;

import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakAdminConsoleIT extends AbstractLiveOakSecurityTest {

    @Test
    public void getAdminResource() throws Exception {
        browser.navigate().to(BASE_ADMIN_URL);

        performLoginWithConfirm("admin", "admin");

        String token = authToken();
        System.out.println("Auth Token: " + token);
    }

}
