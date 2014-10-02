package io.liveoak.scripts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.interceptor.extension.InterceptorExtension;
import io.liveoak.security.extension.SecurityExtension;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import io.netty.buffer.ByteBuf;
import org.vertx.java.core.buffer.Buffer;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public class BaseScriptTestCase extends AbstractHTTPResourceTestCase {

    public final static String BASEPATH = "scripts";

    public final static String SCRIPT_PATH = "/admin/applications/testApp/resources/scripts";

    protected MockInMemoryRootResource rootResource;
    protected File scriptDirectory;

    @Override
    public void loadExtensions() throws Exception {

        //set up Scripting
        //create the directory to store the files
        scriptDirectory = Files.createTempDirectory("io.liveoak.scripts.test").toFile();
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

        //Remove this when LIVEOAK-457 is resolved.
        loadExtension("authz", new SecurityExtension());
        installResource("authz", "authz", JsonNodeFactory.instance.objectNode());

        loadExtension("interceptor", new InterceptorExtension(), createInterceptorConfig());
    }

    protected ObjectNode createInterceptorConfig() {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        ObjectNode authzInterceptor = JsonNodeFactory.instance.objectNode()
                .put("interceptor-name", "authz")
                .put("resource-path-prefix", "/")
                .put("request-type-mapping", "*");
        ObjectNode scriptInterceptor = JsonNodeFactory.instance.objectNode()
                .put("interceptor-name", "script-interceptor");

        ArrayNode httpConfig = config.putArray("http");
        httpConfig.add(authzInterceptor);
        httpConfig.add(scriptInterceptor);

        return config;
    }

    protected ByteBuf readFile(String name) throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("scripts/" + name);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int next = inputStream.read();
        while (next != -1) {
            outputStream.write(next);
            next = inputStream.read();
        }

        return new Buffer(outputStream.toByteArray()).getByteBuf();
    }

    protected ResourceState getMember(ResourceState parent, String id) {
        for (ResourceState resourceState : parent.members()) {
            if (resourceState.id().equals(id)) {
                return resourceState;
            }
        }

        return null;
    }
}
