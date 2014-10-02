package io.liveoak.scripts.resource.scripting;

import java.util.Map;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scripts.JavaScriptResourceState;
import io.liveoak.scripts.resource.BaseResourceTriggeredTestCase;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ClientTestCase extends BaseScriptingTestCase {

    @Before
    public void setupTests() throws Exception {
        //check that there are no other scripts configured
        ResourceState initialState = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH);
        assertThat(initialState.members()).isEmpty();

        ResourceState test = new DefaultResourceState("test");
        test.putProperty("type", "parent");
        test.putProperty("hello", "world");

        client.create(new RequestContext.Builder().build(), "/testApp/mock", test);
        client.create(new RequestContext.Builder().build(), "/testApp/mock", new DefaultResourceState("data"));

        ResourceState foo = new DefaultResourceState("foo");
        foo.putProperty("type", "child");
        foo.putProperty("value", 10);
        client.create(new RequestContext.Builder().build(), "/testApp/mock/test", foo);

        ResourceState bar = new DefaultResourceState("bar");
        bar.putProperty("type", "child");
        bar.putProperty("value", 4);
        client.create(new RequestContext.Builder().build(), "/testApp/mock/test", bar);

        //create the metadata for the script
        ResourceState resourceState = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new BaseResourceTriggeredTestCase.MetadataState("clientTest", "/testApp/mock/*").libraries("client").build());
        assertThat(resourceState).isNotNull();
        assertThat(resourceState.id()).isEqualTo("clientTest");

        //upload the javascript file
        ResourceState binaryResourceState = new JavaScriptResourceState(readFile("testClient.js"));
        ResourceState javascriptState = client.create(new RequestContext.Builder().build(), resourceState.uri().toString(), binaryResourceState);
        assertThat(javascriptState).isNotNull();
    }

    @Test
    public void testDefaultRead() throws Exception {
        // Trigger a read
        get("/testApp/mock/test?test=testDefaultRead").execute();

        ResourceState data = client.read(new RequestContext.Builder().build(), "/testApp/mock/data/testDefaultRead");
        System.out.println("DATA : " + data);

        assertThat(data).isNotNull();
        assertThat(data.getProperty("id")).isEqualTo("test");
        assertThat(data.getProperty("uri")).isEqualTo("/testApp/mock/test");

        Map properties = (Map) data.getProperty("properties");
        assertThat(properties.get("type")).isEqualTo("parent");
        assertThat(properties.get("hello")).isEqualTo("world");

        Map foo = (Map) data.getProperty("member_0");
        assertThat(foo.get("id")).isEqualTo("foo");
        assertThat(foo.get("uri")).isEqualTo("/testApp/mock/test/foo");
        assertThat((Map) foo.get("properties")).isEmpty();

        Map bar = (Map) data.getProperty("member_1");
        assertThat(bar.get("id")).isEqualTo("bar");
        assertThat(bar.get("uri")).isEqualTo("/testApp/mock/test/bar");
        assertThat((Map) bar.get("properties")).isEmpty();
    }

    @Test
    public void testFields() throws Exception {
        // Trigger a read
        execGet("/testApp/mock/test?test=testFields");

        ResourceState data = client.read(new RequestContext.Builder().build(), "/testApp/mock/data/testFields");
        System.out.println("DATA : " + data);

        assertThat(data).isNotNull();
        assertThat(data.getProperty("id")).isEqualTo("test");
        assertThat(data.getProperty("uri")).isEqualTo("/testApp/mock/test");

        Map properties = (Map) data.getProperty("properties");
        assertThat(properties.get("type")).isEqualTo("parent");
        assertThat(properties.get("hello")).isNull();

        Map child1 = (Map) data.getProperty("member_0");
        assertThat(child1.get("id")).isEqualTo("foo");
        assertThat(child1.get("uri")).isEqualTo("/testApp/mock/test/foo");
        Map childProperties1 = (Map) child1.get("properties");
        assertThat(childProperties1.size()).isEqualTo(1);
        assertThat(childProperties1.get("type")).isEqualTo("child");
        assertThat(childProperties1.get("value")).isNull();

        Map child2 = (Map) data.getProperty("member_1");
        assertThat(child2.get("id")).isEqualTo("bar");
        assertThat(child2.get("uri")).isEqualTo("/testApp/mock/test/bar");
        Map childProperties2 = (Map) child2.get("properties");
        assertThat(childProperties2.size()).isEqualTo(1);
        assertThat(childProperties2.get("type")).isEqualTo("child");
        assertThat(childProperties2.get("value")).isNull();
    }

    @Test
    public void testSort() throws Exception {
        // Trigger a read
        execGet("/testApp/mock/test?test=testSort");

        ResourceState data = client.read(new RequestContext.Builder().build(), "/testApp/mock/data/testSort");
        System.out.println("DATA : " + data);

        assertThat(data).isNotNull();
        assertThat(data.getProperty("id")).isEqualTo("test");
        assertThat(data.getProperty("uri")).isEqualTo("/testApp/mock/test");

        Map properties = (Map) data.getProperty("properties");
        assertThat(properties.get("type")).isEqualTo("parent");
        assertThat(properties.get("hello")).isEqualTo("world");

        Map child1 = (Map) data.getProperty("member_0");
        assertThat(child1.get("id")).isEqualTo("bar");
        assertThat(child1.get("uri")).isEqualTo("/testApp/mock/test/bar");

        Map child2 = (Map) data.getProperty("member_1");
        assertThat(child2.get("id")).isEqualTo("foo");
        assertThat(child2.get("uri")).isEqualTo("/testApp/mock/test/foo");
    }

    @Test
    public void testOffset() throws Exception {
        // Trigger a read
        execGet("/testApp/mock/test?test=testOffset");

        ResourceState data = client.read(new RequestContext.Builder().build(), "/testApp/mock/data/testOffset");
        System.out.println("DATA : " + data);

        assertThat(data).isNotNull();
        assertThat(data.getProperty("id")).isEqualTo("test");
        assertThat(data.getProperty("uri")).isEqualTo("/testApp/mock/test");

        Map properties = (Map) data.getProperty("properties");
        assertThat(properties.get("type")).isEqualTo("parent");
        assertThat(properties.get("hello")).isEqualTo("world");

        Map child1 = (Map) data.getProperty("member_0");
        assertThat(child1.get("id")).isEqualTo("bar");
        assertThat(child1.get("uri")).isEqualTo("/testApp/mock/test/bar");

        assertThat(data.getProperty("member_1")).isNull();
    }

    @Test
    public void testLimit() throws Exception {
        // Trigger a read
        execGet("/testApp/mock/test?test=testLimit");

        ResourceState data = client.read(new RequestContext.Builder().build(), "/testApp/mock/data/testLimit");
        System.out.println("DATA : " + data);

        assertThat(data).isNotNull();
        assertThat(data.getProperty("id")).isEqualTo("test");
        assertThat(data.getProperty("uri")).isEqualTo("/testApp/mock/test");

        Map properties = (Map) data.getProperty("properties");
        assertThat(properties.get("type")).isEqualTo("parent");
        assertThat(properties.get("hello")).isEqualTo("world");

        Map child1 = (Map) data.getProperty("member_0");
        assertThat(child1.get("id")).isEqualTo("foo");
        assertThat(child1.get("uri")).isEqualTo("/testApp/mock/test/foo");

        assertThat(data.getProperty("member_1")).isNull();
    }


}
