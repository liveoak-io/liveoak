package org.liveoak.testsuite;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.liveoak.testsuite.annotations.Resource;
import org.liveoak.testsuite.junit.LiveOak;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@RunWith(LiveOak.class)
public class BasicIT {

    @Resource
    private HttpClient client;

    @Resource
    private URL url;

    @Test
    public void testAppIndexHtml() throws Exception {
        String resource = getResource(client, new URL(url, "/liveoak/default/app/index.html"));

        Assert.assertNotNull( resource );
        resource = resource.trim();

        Assert.assertTrue(resource.startsWith("<!DOCTYPE html>"));
        Assert.assertTrue(resource.contains("<title>testsuite</title>"));
        Assert.assertTrue(resource.endsWith("</html>"));
    }

    @Test
    public void testClientJS() throws Exception {
        String js = getResource(client, new URL(url, "/liveoak/zero/client/liveoak.js"));

        Assert.assertTrue(js.contains("var LiveOak = function"));
        Assert.assertTrue(js.contains("var Keycloak = function"));
        Assert.assertTrue(js.contains("var Http = function"));
        Assert.assertTrue(js.contains("var Stomp"));
    }

    private String getResource(HttpClient client, URL resource) throws IOException, URISyntaxException {
        HttpGet appGet = new HttpGet(resource.toURI());
        HttpResponse response = client.execute(appGet);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        return IOUtils.toString(response.getEntity().getContent());
    }

}
