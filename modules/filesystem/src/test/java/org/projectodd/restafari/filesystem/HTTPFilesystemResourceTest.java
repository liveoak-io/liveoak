package org.projectodd.restafari.filesystem;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Ignore;
import org.junit.Test;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.spi.Config;
import org.projectodd.restafari.spi.resource.RootResource;
import org.projectodd.restafari.testtools.AbstractHTTPResourceTestCase;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class HTTPFilesystemResourceTest extends AbstractHTTPResourceTestCase {

    @Override
    public RootResource createRootResource() {
        return new FilesystemResource("files");
    }

    @Override
    public Config createConfig() {
        SimpleConfig config = new SimpleConfig();
        //config.put( "root", "./test-data/one" );
        config.put( "root", "./test-data/one" );
        return config;
    }


    @Test
    @Ignore
    public void testEnumerateRoot() throws Exception {

        HttpGet get = new HttpGet("http://localhost:8080/files");
        get.addHeader("Accept", "application/json");

        try {
            System.err.println("DO GET");
            CloseableHttpResponse result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            HttpEntity entity = result.getEntity();
            if (entity.getContentLength() > 0) {
                entity.writeTo(System.err);
            }
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);

        } finally {
            httpClient.close();
        }

    }
}
