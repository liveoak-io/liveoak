package io.liveoak.testsuite.console;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.jboss.arquillian.graphene.Graphene.*;
import static org.junit.Assert.*;

//import org.jboss.arquillian.graphene.angular.findby.FindByNg;

/**
 * @author <a href="mailto:amendonc@redhat.com">Alexandre Mendonca</a>
 */
@RunWith(Arquillian.class)
public class ApplicationsPageIT {

    @Drone
    WebDriver browser;

    private static final String BASE_URL = "http://localhost:8080/admin/";

    @FindBy
    private WebElement username;

    @FindBy
    private WebElement password;

    @FindBy(id = "kc-login")
    private WebElement loginButton;

    @FindBy(className = "empty-instance")
    private WebElement noAppsPanel;

    @FindBy(id = "btn-create-app-noapps")
    //@FindByNg(action = "modalApplicationCreate()")
    private WebElement createAppBtn;

    private static final String APP_NAME = "TestAppOne";
    private static final String DEFAULT_STORAGE_NAME = "storage";
    private static final String PUSH_URL = "localhost";
    private static final String PUSH_APPID = "the-app-lica-tion";
    private static final String PUSH_SECRET = "terces";

    public void login(String userName, String password) {
        this.username.sendKeys(userName);
        this.password.sendKeys(password);
        //loginButton.click();
        //waitModel().until().element(noAppsPanel).is().visible();
        guardHttp(loginButton).click();
    }

    private void openNewApplicationModalWithNoApps() {
        waitGui().until().element(createAppBtn).is().visible();
        createAppBtn.click();
        assertTrue("Check that Create Application modal is displayed", browser.findElement(By.id("step1")).isDisplayed());
    }


    private void closeNewApplicationModalWithCancel() {
        assertTrue("Check that 'Cancel' button is displayed", browser.findElement(By.id("step1")).isDisplayed());
        createAppBtn.click();
        assertTrue("Check that Create Application modal is displayed", browser.findElement(By.id("step1")).isDisplayed());
    }

    private void closeNewApplicationModalWithBackdrop() {
        waitGui().until().element(createAppBtn).is().visible();
        createAppBtn.click();
        assertTrue("Check that Create Application modal is displayed", browser.findElement(By.id("step1")).isDisplayed());
    }

    private void closeNewApplicationModalWithEscape() {
        waitGui().until().element(createAppBtn).is().visible();
        createAppBtn.click();
        assertTrue("Check that Create Application modal is displayed", browser.findElement(By.id("step1")).isDisplayed());
    }

    @Before
    public void before() throws Exception {
        browser.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
    }

    @Test
    @InSequence(1)
    public void checkNoAppsInstalledExists() {
        browser.navigate().to(BASE_URL);

        login("admin", "admin");

        assertTrue("Check that page contains No Applications element", noAppsPanel.isDisplayed());
        assertEquals("Check that No Applications element has correct text", "LiveOak currently has no applications installed.", noAppsPanel.findElement(By.tagName("p")).getText());
    }

    @Test
    @InSequence(2)
    public void openAndCloseNewApplicationModal() {
        browser.navigate().refresh();

        openNewApplicationModalWithNoApps();

        waitAjax().until().element(By.id("step1")).is().visible();
        // FIXME: guardAjax(createAppBtn).click();

        assertTrue("Check that we are on Step 1/3", browser.findElement(By.className("steps")).getText().contains(" 1/3"));

        assertTrue("Check that Application Name is present", browser.findElement(By.id("application-name")).isDisplayed());
        assertEquals("Check that Application Name has focus", "application-name", browser.switchTo().activeElement().getAttribute("id"));
        assertEquals("Check that Application Name is empty", "", browser.findElement(By.id("application-name")).getAttribute("value"));

        // Test Setup Type behavior

        assertTrue("Check that Type of Setup 'Basic' is present", browser.findElement(By.id("radio-app-basic")).isDisplayed());
        assertTrue("Check that Type of Setup 'Do-It-Yourself' is present", browser.findElement(By.id("radio-app-diy")).isDisplayed());

        assertTrue("Check that Type of Setup 'Basic' is enabled", browser.findElement(By.id("radio-app-basic")).isEnabled());
        assertTrue("Check that Type of Setup 'Do-It-Yourself' is enabled", browser.findElement(By.id("radio-app-diy")).isEnabled());

        assertTrue("Check that Type of Setup 'Basic' is selected", browser.findElement(By.id("radio-app-basic")).isSelected());

        String basicInfoText = "";
        for(WebElement infoText : browser.findElements(By.className("alert-info"))) {
            if(infoText.isDisplayed()) {
                assertTrue("Check that only one Info Text is displayed at a time", basicInfoText.isEmpty());
                // TODO: Check if it's the right one
                basicInfoText = infoText.getText();
            }
        }

        browser.findElement(By.id("radio-app-diy")).click();

        assertTrue("Check that Type of Setup 'Do-It-Yourself' is selected", browser.findElement(By.id("radio-app-diy")).isSelected());

        String diyInfoText = "";
        for(WebElement infoText : browser.findElements(By.className("alert-info"))) {
            if(infoText.isDisplayed()) {
                assertTrue("Check that only one Info Text is displayed at a time", diyInfoText.isEmpty());
                // TODO: Check if it's the right one
                diyInfoText = infoText.getText();
            }
        }

        assertNotEquals("Check that Info text changes when changing Type of Setup", basicInfoText, diyInfoText);

        // Check buttons presence and behavior

        assertTrue("Check that 'Next' button is not present", browser.findElements(By.id("btn-setup-next")).isEmpty());
        assertFalse("Check that 'Previous' button is not present", browser.findElement(By.id("btn-setup-prev")).isDisplayed()); // FIXME better way ?
        assertTrue("Check that 'Create' button is present", browser.findElement(By.id("btn-setup-create")).isDisplayed());
        assertTrue("Check that 'Cancel' button is present", browser.findElement(By.id("btn-setup-cancel")).isDisplayed());

        assertTrue("Check that 'Cancel' button is enabled", browser.findElement(By.id("btn-setup-cancel")).isEnabled());
        assertFalse("Check that 'Create' button is disabled when name is empty", browser.findElement(By.id("btn-setup-create")).isEnabled());

        browser.findElement(By.id("application-name")).sendKeys(APP_NAME);

        assertEquals("Check that Application Name is '" + APP_NAME + "'", APP_NAME, browser.findElement(By.id("application-name")).getAttribute("value"));

        for(char c : APP_NAME.toCharArray()) {
            assertTrue("Check that 'Cancel' button is enabled", browser.findElement(By.id("btn-setup-cancel")).isEnabled());
            assertTrue("Check that 'Create' button is enabled when name is filled", browser.findElement(By.id("btn-setup-create")).isEnabled());
            browser.findElement(By.id("application-name")).sendKeys(Keys.BACK_SPACE);
        }

        assertTrue("Check that 'Cancel' button is enabled", browser.findElement(By.id("btn-setup-cancel")).isEnabled());
        assertFalse("Check that 'Create' button is disabled when name is empty", browser.findElement(By.id("btn-setup-create")).isEnabled());

        // Back to Basic Setup Type
        browser.findElement(By.id("radio-app-basic")).click();
        // Check buttons presence and behavior

        assertTrue("Check that 'Next' button is present", browser.findElement(By.id("btn-setup-next")).isDisplayed());
        assertFalse("Check that 'Previous' button is not present", browser.findElement(By.id("btn-setup-prev")).isDisplayed()); // FIXME better way ?
        assertTrue("Check that 'Create' button is not present", browser.findElements(By.id("btn-setup-create")).isEmpty()); // FIXME better way ?
        assertTrue("Check that 'Cancel' button is present", browser.findElement(By.id("btn-setup-cancel")).isDisplayed());

        assertTrue("Check that 'Cancel' button is enabled", browser.findElement(By.id("btn-setup-cancel")).isEnabled());
        assertFalse("Check that 'Next' button is disabled when name is empty", browser.findElement(By.id("btn-setup-next")).isEnabled());

        browser.findElement(By.id("application-name")).sendKeys(APP_NAME);

        assertEquals("Check that Application Name is '" + APP_NAME + "'", APP_NAME, browser.findElement(By.id("application-name")).getAttribute("value"));

        for(char c : APP_NAME.toCharArray()) {
            assertTrue("Check that 'Cancel' button is enabled", browser.findElement(By.id("btn-setup-cancel")).isEnabled());
            assertTrue("Check that 'Next' button is enabled when name is filled", browser.findElement(By.id("btn-setup-next")).isEnabled());
            browser.findElement(By.id("application-name")).sendKeys(Keys.BACK_SPACE);
        }

        assertTrue("Check that 'Cancel' button is enabled", browser.findElement(By.id("btn-setup-cancel")).isEnabled());
        assertFalse("Check that 'Next' button is disabled when name is empty", browser.findElement(By.id("btn-setup-next")).isEnabled());

        browser.findElement(By.id("application-name")).sendKeys(APP_NAME);
        browser.findElement(By.id("btn-setup-next")).click();

        assertTrue("Check that we are on Step 2/3", browser.findElement(By.className("steps")).getText().contains(" 2/3"));

        assertTrue("Check that Storage URL is present", browser.findElement(By.id("dburl")).isDisplayed());
        assertEquals("Check that Storage URL has focus", "dburl", browser.switchTo().activeElement().getAttribute("id"));
        assertEquals("Check that Storage URL value is '" + DEFAULT_STORAGE_NAME + "'", DEFAULT_STORAGE_NAME, browser.findElement(By.id("dburl")).getAttribute("value"));
        //assertTrue("Check that Storage URL value is selected", browser.findElement(By.id("dburl")).isSelected());

        assertTrue("Check that 'Next' button is present", browser.findElement(By.id("btn-setup-next")).isDisplayed());
        assertTrue("Check that 'Previous' button is present", browser.findElement(By.id("btn-setup-prev")).isDisplayed());
        assertTrue("Check that 'Create' button is not present", browser.findElements(By.id("btn-setup-create")).isEmpty()); // FIXME better way ?
        assertFalse("Check that 'Cancel' button is not present", browser.findElement(By.id("btn-setup-cancel")).isDisplayed()); // FIXME better way ?

        assertTrue("Check that 'Previous' button is enabled", browser.findElement(By.id("btn-setup-prev")).isEnabled());
        assertTrue("Check that 'Next' button is enabled when storage is filled", browser.findElement(By.id("btn-setup-next")).isEnabled());

        browser.findElement(By.id("dburl")).sendKeys(Keys.BACK_SPACE);

        // if text was selected, everything should be gone

        assertEquals("Check that Storage URL value is empty after one backspace", "", browser.findElement(By.id("dburl")).getAttribute("value"));

        assertTrue("Check that 'Previous' button is enabled", browser.findElement(By.id("btn-setup-prev")).isEnabled());
        assertFalse("Check that 'Next' button is not enabled when storage is empty", browser.findElement(By.id("btn-setup-next")).isEnabled());

        browser.findElement(By.id("dburl")).sendKeys(DEFAULT_STORAGE_NAME);

        assertTrue("Check that 'Previous' button is enabled", browser.findElement(By.id("btn-setup-cancel")).isEnabled());
        assertTrue("Check that 'Next' button is enabled when storage is filled", browser.findElement(By.id("btn-setup-next")).isEnabled());

        browser.findElement(By.id("btn-setup-next")).click();

        assertTrue("Check that we are on Step 3/3", browser.findElement(By.className("steps")).getText().contains(" 3/3"));

        assertTrue("Check that Push URL is present", browser.findElement(By.id("pushUrl")).isDisplayed());
        assertTrue("Check that Push Application ID is present", browser.findElement(By.id("appId")).isDisplayed());
        assertTrue("Check that Push Secret is present", browser.findElement(By.id("secret")).isDisplayed());

        assertEquals("Check that Push URL has focus", "pushUrl", browser.switchTo().activeElement().getAttribute("id"));

        assertEquals("Check that Push URL value is empty", "", browser.findElement(By.id("pushUrl")).getAttribute("value"));
        assertEquals("Check that Push Application ID value is empty", "", browser.findElement(By.id("appId")).getAttribute("value"));
        assertEquals("Check that Push Secret value is empty", "", browser.findElement(By.id("secret")).getAttribute("value"));

        assertTrue("Check that 'Next' button is not present", browser.findElements(By.id("btn-setup-next")).isEmpty());
        assertTrue("Check that 'Previous' button is present", browser.findElement(By.id("btn-setup-prev")).isDisplayed());
        assertTrue("Check that 'Create' button is present", browser.findElement(By.id("btn-setup-create")).isDisplayed());
        assertFalse("Check that 'Cancel' button is not present", browser.findElement(By.id("btn-setup-cancel")).isDisplayed());

        assertTrue("Check that 'Previous' button is enabled", browser.findElement(By.id("btn-setup-prev")).isEnabled());
        assertTrue("Check that 'Create' button is enabled", browser.findElement(By.id("btn-setup-create")).isEnabled());

        browser.findElement(By.id("pushUrl")).sendKeys(PUSH_URL);

        // FIXME: assertEquals("Check that Push URL value is 'http://" + PUSH_URL + "'", "http://" + PUSH_URL, browser.findElement(By.id("pushUrl")).getAttribute("value"));

        assertTrue("Check that 'Previous' button is enabled", browser.findElement(By.id("btn-setup-prev")).isEnabled());
        assertFalse("Check that 'Create' button is disabled when only Push URL is filled", browser.findElement(By.id("btn-setup-create")).isEnabled());

        browser.findElement(By.id("appId")).sendKeys(PUSH_APPID);

        assertEquals("Check that Push App ID value is '" + PUSH_APPID + "'", PUSH_APPID, browser.findElement(By.id("appId")).getAttribute("value"));

        assertTrue("Check that 'Previous' button is enabled", browser.findElement(By.id("btn-setup-prev")).isEnabled());
        assertFalse("Check that 'Create' button is disabled when only Push URL and App ID are filled", browser.findElement(By.id("btn-setup-create")).isEnabled());

        browser.findElement(By.id("secret")).sendKeys(PUSH_SECRET);

        assertEquals("Check that Push Secret value is '" + PUSH_SECRET + "'", PUSH_SECRET, browser.findElement(By.id("secret")).getAttribute("value"));

        assertTrue("Check that 'Previous' button is enabled", browser.findElement(By.id("btn-setup-prev")).isEnabled());
        assertTrue("Check that 'Create' button is enabled when all push config is filled", browser.findElement(By.id("btn-setup-create")).isEnabled());

    }

}
