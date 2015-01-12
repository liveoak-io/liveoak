package io.liveoak.testsuite;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakAdminConsoleIT extends AbstractLiveOakTest {

    protected String keycloakUrl;

    @Before
    public void before() throws Exception {
        super.before();

        keycloakUrl = BASE_URL + "/auth/rest";
    }

    @Test
    public void getAdminResource() throws Exception {
        browser.get(BASE_URL + "/admin");

        System.out.println(browser.getTitle());

        browser.findElement(By.id("username")).sendKeys("admin");
        browser.findElement(By.id("password")).sendKeys("admin");

        browser.findElement(By.id("kc-login")).click();

        Thread.sleep(2000);

        String getToken = "return angular.element(document).injector().get('LiveOak').auth.token";

        JavascriptExecutor js = (JavascriptExecutor) browser;
        String token = (String) js.executeScript(getToken);

        System.out.println(token);
    }

}
