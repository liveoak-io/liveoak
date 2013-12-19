package org.liveoak.testsuite.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.liveoak.testsuite.junit.LiveOak;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class JsExecutor {

    private LiveOak.WebDriverResource driver;

    public JsExecutor(LiveOak.WebDriverResource driver) {
        this.driver = driver;
    }

    public Object execute(String script, Object... args) throws IOException, JSONException {
        InputStream is = System.class.getResourceAsStream("/" + script);
        script = IOUtils.toString(is);
        JavascriptExecutor executor = (JavascriptExecutor) driver.get();
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
