package org.liveoak.testsuite.js;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.liveoak.testsuite.annotations.Resource;
import org.liveoak.testsuite.junit.LiveOak;
import org.liveoak.testsuite.utils.JsExecutor;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.net.URL;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@RunWith(LiveOak.class)
public class JsHttpIT {

    @Resource
    private WebDriver driver;

    @Resource
    private JsExecutor js;

    @Resource
    private URL url;

    @Before
    public void before() throws IOException, JSONException {
        driver.navigate().to(new URL(url, "/app/index.html"));
        js.execute("init-liveoak.js");
    }

    @Test
    public void testReadMembers() throws IOException, JSONException {
        JSONArray result = (JSONArray) js.execute("read-members.js", "/app");

        Assert.assertTrue(result.length() > 0);
        Assert.assertEquals(1, result.length());
        Assert.assertEquals("index.html", result.getJSONObject(0).getString("id"));
    }

    @Test
    public void testRead() throws IOException, JSONException {
        JSONObject result = (JSONObject) js.execute("read.js", "/app");

        Assert.assertEquals("app", result.getString("id"));
        Assert.assertEquals("/app", result.getJSONObject("self").getString("href"));
    }

}
