package io.liveoak.testsuite.js;

import java.io.IOException;
import java.net.URL;

import io.liveoak.testsuite.AbstractLiveOakTest;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@Ignore
public class JsHttpIT extends AbstractLiveOakTest {

    @Drone
    private WebDriver browser;

    @Drone
    private JavascriptExecutor js;

    @Before
    public void before() throws Exception {
        browser.navigate().to(new URL(BASE_URL + "/default/app/index.html"));
        execute("init-liveoak.js");
    }

    @Test
    public void testReadMembers() throws IOException, JSONException {
        JSONArray result = (JSONArray) execute("read-members.js", "/default/app");

        System.err.println( "RESULT: " + result );
        Assert.assertTrue(result.length() > 0);
        Assert.assertEquals(1, result.length());
        Assert.assertEquals("index.html", result.getJSONObject(0).getString("id"));
    }

    @Test
    public void testRead() throws IOException, JSONException {
        JSONObject result = (JSONObject) execute("read.js", "/default");

        Assert.assertEquals("default", result.getString("id"));
        Assert.assertEquals("/default", result.getJSONObject("self").getString("href"));
    }

}
