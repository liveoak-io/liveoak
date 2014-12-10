package io.liveoak.redirect.https;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.spi.util.ObjectMapperFactory;
import io.liveoak.interceptor.extension.InterceptorExtension;
import io.liveoak.security.extension.SecurityExtension;
import io.liveoak.security.policy.uri.extension.URIPolicyExtension;
import io.liveoak.testtools.AbstractHTTPResourceTestCaseWithTestApp;
import io.liveoak.testtools.resources.MockInMemoryRootResource;
import org.junit.BeforeClass;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseHttpsRedirectTestCase extends AbstractHTTPResourceTestCaseWithTestApp {

    public final static String BASEPATH = "https-redirect";

    protected static MockInMemoryRootResource rootResource;

    @BeforeClass
    public static void loadExtensions() throws Exception {

        //configure and load the extension

        JsonNode config = ObjectMapperFactory.create().readTree(
                " {'default': {" +
                "        'redirects': 'SECURED',\n" +
                "        'redirect-type': 'TEMPORARY',\n" +
                "        'max-age': 300\n" +
                "      }\n}");

        //load the extension and install the resource
        loadExtension("https-redirect", new RedirectExtension(), (ObjectNode) config);
        installTestAppResource("https-redirect", BASEPATH, JsonNodeFactory.instance.objectNode());

        //setup a mock resource to be used
        rootResource = new MockInMemoryRootResource("mock");
        loadExtension("mock", new MockExtension(rootResource));
        installTestAppResource("mock", "mock", JsonNodeFactory.instance.objectNode());

        JsonNode authzConfig = ObjectMapperFactory.create().readTree(" {\n" +
                "        \"policies\" : [ {\n" +
                "          \"policyName\" : \"URIPolicy\",\n" +
                "          \"policyResourceEndpoint\" : \"/testApp/uri-policy/authzCheck\"\n" +
                "        } ]\n" +
                "      }");


        loadExtension("authz", new SecurityExtension());
        installTestAppResource("authz", "authz", (ObjectNode)authzConfig);

        loadExtension("uri-policy", new URIPolicyExtension());
        installTestAppResource("uri-policy", "uri-policy", getPolicyConfig());

        loadExtension("interceptor", new InterceptorExtension(), createInterceptorConfig());
    }

    private static ObjectNode getPolicyConfig() throws Exception {
        ObjectMapper om = ObjectMapperFactory.create();
        ObjectNode objectNode = om.readValue(BaseHttpsRedirectTestCase.class.getClassLoader().getResourceAsStream("policy-config/basic-uri-policy-config.json"), ObjectNode.class);
        return objectNode;
    }

    protected static ObjectNode createInterceptorConfig() {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        ObjectNode authzInterceptor = JsonNodeFactory.instance.objectNode()
                .put("interceptor-name", "authz")
                .put("resource-path-prefix", "/")
                .put("request-type-mapping", "*");
        ObjectNode redirectInterceptor = JsonNodeFactory.instance.objectNode()
                .put("interceptor-name", "https-redirect");

        ArrayNode httpConfig = config.putArray("http");
        httpConfig.add(redirectInterceptor);
        httpConfig.add(authzInterceptor);

        return config;
    }

}
