package io.liveoak.scripts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.interceptor.extension.InterceptorExtension;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import io.netty.buffer.ByteBuf;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.vertx.java.core.buffer.Buffer;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseScriptTestCase extends AbstractHTTPResourceTestCase {

    public final static String BASEPATH = "scripts";

    public final static String SCRIPT_PATH = "/admin/applications/testApp/resources/scripts";

    protected MockInMemoryRootResource rootResource;

    @Override
    public void loadExtensions() throws Exception {

        //set up Scripting
        //create the directory to store the files
        File scriptDirectory = Files.createTempDirectory("io.liveoak.scripts.test").toFile();
        scriptDirectory.mkdir();
        //configure and load the extension
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.put("script-directory", scriptDirectory.toString());
        //load the extension and install the resource
        loadExtension("scripts", new ScriptExtension(), JsonNodeFactory.instance.objectNode());
        installResource("scripts", BASEPATH, config);

        //setup a mock resource to be used
        rootResource = new MockInMemoryRootResource("mock");
        loadExtension("mock", new MockExtension(rootResource));
        installResource("mock", "mock", JsonNodeFactory.instance.objectNode());


        loadExtension("interceptor", new InterceptorExtension(), createInterceptorConfig());

    }

    protected ObjectNode createInterceptorConfig() {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        ObjectNode localChainConfig = JsonNodeFactory.instance.objectNode()
                .put("interceptor-name", "script-interceptor");
        config.putArray("http").add(localChainConfig);
        return config;
    }

    public JsonNode getHttpResource(String uri) throws Exception {
        HttpGet get = new HttpGet(uri);
        get.addHeader("Accept", "application/json");
        try {
            CloseableHttpResponse result = httpClient.execute(get);

            HttpEntity entity = result.getEntity();

            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);

            JsonNode jsonNode = ObjectMapperFactory.create().readTree(entity.getContent());

            return jsonNode;

        } finally {
            httpClient.close();
        }
    }

//    public JsonNode postHttpResource(String uri, JsonNode data) throws Exception {
//        HttpPost post = new HttpPost(uri);
//        post.addHeader("Accept", "application/json");
//        post.addHeader("Content-Type", "application/json");
//        try {
//            System.err.println("FOO1");
//            post.setEntity(new StringEntity(data.asText()));
//            System.err.println("FOO2");
//            CloseableHttpResponse result = httpClient.execute(post);
//            System.err.println("FOO4");
//            result.close();
//            HttpEntity entity = result.getEntity();
//
//            System.err.println("FOO4");
//            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
//            System.err.println("FOO5");
//            JsonNode jsonNode = ObjectMapperFactory.create().readTree(entity.getContent());
//            System.err.println("FOO6");
//
//            return jsonNode;
//
//        } finally {
//            httpClient.close();
//        }
//    }

    public ByteBuf readFile(String name) throws Exception  {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("scripts/" + name);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int next = inputStream.read();
        while (next != -1) {
            outputStream.write(next);
            next = inputStream.read();
        }

        return new Buffer(outputStream.toByteArray()).getByteBuf();
    }

    public ResourceState getMember(ResourceState parent, String id) {
        for (ResourceState resourceState: parent.members()) {
            if (resourceState.id().equals(id)) {
                return resourceState;
            }
        }

        return null;
    }
}
