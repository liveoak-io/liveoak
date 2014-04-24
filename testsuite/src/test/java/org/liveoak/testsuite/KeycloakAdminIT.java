package org.liveoak.testsuite;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@Ignore
public class KeycloakAdminIT extends AbstractLiveOakTest {

    protected String keycloakUrl;

    @Drone
    private WebDriver browser;

    @Before
    public void before() throws Exception {
        super.before();

        keycloakUrl = baseUrl.replace("8080", "8383") + "/auth/rest";
    }

    @Test
    public void getAdminResource() throws Exception {
        browser.get(baseUrl + "/admin");

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
