package io.liveoak.testsuite.security;

import io.liveoak.testsuite.AbstractLiveOakTest;
import org.openqa.selenium.JavascriptExecutor;

/**
 * @author Ken Finnigan
 */
public abstract class AbstractLiveOakSecurityTest extends AbstractLiveOakTest {
    protected static final String BASE_SECURITY_URL = BASE_URL + "/auth";

    protected static final String BASE_SECURITY_ADMIN_URL = BASE_SECURITY_URL + "/admin";

    protected String authToken() {
        String getToken = "return angular.element(document).injector().get('LiveOak').auth.token";

        JavascriptExecutor js = (JavascriptExecutor) browser;
        return (String) js.executeScript(getToken);
    }
}
