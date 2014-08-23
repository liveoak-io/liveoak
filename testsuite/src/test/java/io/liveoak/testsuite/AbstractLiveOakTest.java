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
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public abstract class AbstractLiveOakTest {

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected CloseableHttpClient client;

    protected String baseUrl;

    @Drone
    protected WebDriver browser;

    @Before
    public void before() throws Exception {
        client = HttpClients.createDefault();
        baseUrl = "http://localhost:8080";
    }

    @After
    public void after() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    public JsonNode get(String resource) throws IOException {
        HttpGet get = new HttpGet(baseUrl + resource);
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
