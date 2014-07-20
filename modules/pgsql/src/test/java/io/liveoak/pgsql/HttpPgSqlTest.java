package io.liveoak.pgsql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class HttpPgSqlTest extends BasePgSqlHttpTest {

    protected static final String APPLICATION_JSON = "application/json";

    @Before
    public void init() throws IOException {

        // create three tables
        HttpPost post = new HttpPost("http://localhost:8080/testApp/" + BASEPATH);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String json = "{" +
                "  \"id\": \"addresses\",\n" +
                "  \"columns\": [\n" +
                "     {\n" +
                "       \"name\": \"address_id\",\n" +
                "       \"type\": \"integer\"\n" +
                "     },\n" +
                "     {\n" +
                "       \"name\": \"name\",\n" +
                "       \"type\": \"varchar\",\n" +
                "       \"size\": 255,\n" +
                "       \"nullable\": false\n" +
                "     },\n" +
                "     {\n" +
                "       \"name\": \"street\",\n" +
                "       \"type\": \"varchar\",\n" +
                "       \"size\": 255,\n" +
                "       \"nullable\": false\n" +
                "     },\n" +
                "     {\n" +
                "       \"name\": \"postcode\",\n" +
                "       \"type\": \"varchar\",\n" +
                "       \"size\": 10\n" +
                "     },\n" +
                "     {\n" +
                "       \"name\": \"city\",\n" +
                "       \"type\": \"varchar\",\n" +
                "       \"size\": 60,\n" +
                "       \"nullable\": false\n" +
                "     },\n" +
                "     {\n" +
                "       \"name\": \"country_iso\",\n" +
                "       \"type\": \"char\",\n" +
                "       \"size\": 2\n" +
                "     },\n" +
                "     {\n" +
                "       \"name\": \"is_company\",\n" +
                "       \"type\": \"boolean\",\n" +
                "       \"nullable\": false,\n" +
                "       \"default\": false\n" +
                "     }],\n" +
                "  \"primary-key\": [\"address_id\"]\n" +
                "}";

        System.out.println("Creating table: " + json);

        StringEntity entity = new StringEntity(json, ContentType.create(APPLICATION_JSON, "UTF-8"));
        post.setEntity(entity);

        System.err.println("DO PUT - " + post.getURI());

        CloseableHttpResponse result = httpClient.execute(post);

        System.err.println("=============>>>");
        System.err.println(result);

        HttpEntity resultEntity = result.getEntity();

        assertThat(resultEntity.getContentLength()).isGreaterThan(0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resultEntity.writeTo(baos);

        String resultStr = new String(baos.toByteArray());
        System.err.println(resultStr);
        JsonObject resultJson = new JsonObject(resultStr);

        System.out.println("resultJson: " + resultJson);
        System.err.println("\n<<<=============");
    }

    @After
    public void cleanup() {

    }

    @Test
    public void testAll() {
        System.out.println("testAll");
    }
}
