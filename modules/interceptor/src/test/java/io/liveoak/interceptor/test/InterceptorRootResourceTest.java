package io.liveoak.interceptor.test;

import java.util.List;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.interceptor.extension.InterceptorExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InterceptorRootResourceTest extends AbstractResourceTestCase {

    @Override
    public void loadExtensions() throws Exception {
        loadExtension("interceptor", new InterceptorExtension(), createConfig());
        loadExtension("mock-interceptor", new MockInterceptorExtension());
    }

    protected ObjectNode createConfig() {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        ObjectNode localChainConfig = JsonNodeFactory.instance.objectNode()
                .put("interceptor-name", "mock-interceptor")
                .put("resource-path-prefix", "/testApp");
        config.putArray("local").add(localChainConfig);
        return config;
    }

    @Test
    public void testInterceptorConfig() throws Exception {
        RequestContext requestContext = new RequestContext.Builder().build();
        client.read(requestContext, "/testApp");
        client.read(requestContext, "/testApp/subscriptions");
        ResourceState adminState = client.read(requestContext, "/admin/system/interceptor/module");
        assertCounter(2);

        // Assert configuration is expected
        List<ResourceState> interceptorsConfig = (List<ResourceState>)adminState.getProperty("local");
        Assert.assertEquals(1, interceptorsConfig.size());
        Assert.assertEquals("mock-interceptor", interceptorsConfig.get(0).getProperty("interceptor-name"));
        Assert.assertEquals("/testApp", interceptorsConfig.get(0).getProperty("resource-path-prefix"));

        // Disable mapping for mockInterceptor
        interceptorsConfig.get(0).putProperty("resource-path-prefix", "/testApp/something-which-does-not-exist");
        client.update(requestContext, "/admin/system/interceptor/module", adminState);

        // This request won't reach mockInterceptor and so won't increase counter
        client.read(requestContext, "/testApp");

        // Re-enable mapping for mockInterceptor again
        interceptorsConfig.get(0).putProperty("resource-path-prefix", "/testApp");
        client.update(requestContext, "/admin/system/interceptor/module", adminState);

        // Counter still 2
        assertCounter(2);

        // Now counter will be increased
        client.read(requestContext, "/testApp");
        assertCounter(3);
    }

    private void assertCounter(int expectedCounter) throws Exception {
        ResourceState normalState = client.create(new RequestContext.Builder().build(), "/testApp/baz", new DefaultResourceState());
        Assert.assertEquals(expectedCounter, normalState.getProperty("requestCounter"));
        Assert.assertEquals(expectedCounter, normalState.getProperty("responseCounter"));
    }
}
