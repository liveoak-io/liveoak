package io.liveoak.testsuite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.FindBy;

import java.io.IOException;
import java.io.InputStream;

import static org.jboss.arquillian.graphene.Graphene.guardHttp;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public abstract class AbstractLiveOakTest {

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected CloseableHttpClient client;

    protected static final String BASE_URL = "http://localhost:8080";
    protected static final String BASE_ADMIN_URL = BASE_URL + "/admin/";

    @Drone
    protected WebDriver browser;

    @FindBy
    protected WebElement username;

    @FindBy
    protected WebElement password;

    @FindBy(id = "kc-login")
    protected WebElement loginButton;

    @FindBy(id = "password-new")
    protected WebElement passwordNew;

    @FindBy(id = "password-confirm")
    protected WebElement passwordConfirm;

    @FindBy(xpath = "//input[@type='submit']")
    private WebElement submitPasswordConfirm;

    @Before
    public void before() throws Exception {
        client = HttpClients.createDefault();
    }

    @After
    public void after() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    /**
     * Output the content within the logType. Use {@link org.openqa.selenium.logging.LogType} to specify
     * which logType to output.
     *
     * @param logType
     */
    protected void outputLog(String logType) {
        for (LogEntry o : browser.manage().logs().get(logType).getAll()) {
            System.out.println(logType + " Log: " + o.getMessage());
        }
    }

    protected void performLogin(String username, String password) {
        this.username.clear();
        this.username.sendKeys(username);
        this.password.clear();
        this.password.sendKeys(password);

        guardHttp(loginButton).click();
    }

    protected void performLoginWithConfirm(String username, String password) {
        performLogin(username, password);

        try {
            if (this.passwordNew.isDisplayed()) {
                this.passwordNew.clear();
                this.passwordNew.sendKeys(password);
                this.passwordConfirm.clear();
                this.passwordConfirm.sendKeys(password);

                guardHttp(submitPasswordConfirm).click();
            }
        } catch (NoSuchElementException e) {
            // Ignore as it means user account has already updated the password on a previous login
        }
    }

    public JsonNode get(String resource) throws IOException {
        HttpGet get = new HttpGet(BASE_URL + resource);
        get.setHeader(HttpHeaders.ACCEPT, "application/json");
        try (CloseableHttpResponse response = client.execute(get)) {
            return objectMapper.readTree(response.getEntity().getContent());
        }
    }

    public Object execute(String script, Object... args) throws IOException, JSONException {
        InputStream is = System.class.getResourceAsStream("/" + script);
        script = IOUtils.toString(is);
        JavascriptExecutor executor = (JavascriptExecutor) browser;
        Object result = executor.executeAsyncScript(script, args);
        if (result == null) {
            return null;
        }

        if (result instanceof String) {
            String s = (String) result;
            s = s.trim();
            if (s.startsWith("[")) {
                return new JSONArray(s);
            } else if (s.startsWith("{")) {
                return new JSONObject(s);
            } else {
                return s;
            }
        }

        return result;
    }

}
