package io.liveoak.scripts;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.UpdateNotSupportedException;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests the main scripting endpoint
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public class ScriptTestCase extends BaseScriptTestCase {

    @Test
    public void testScriptingEndpoint() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), SCRIPT_PATH);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("scripts");

        assertThat(getMember(result, "resource-triggered-scripts")).isNotNull();
        assertThat(getMember(result, "scheduled-scripts")).isNotNull();
    }

    @Test(expected = UpdateNotSupportedException.class)
    public void testUnableToUpdateScriptResourceDirectory() throws Exception {
        client.update(new RequestContext.Builder().build(), SCRIPT_PATH, new DefaultResourceState());
    }

    @Test
    public void testReadScriptResourceDirectory() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), SCRIPT_PATH);
        assertThat(result).isNotNull();
        assertThat(result.getProperty("script-directory")).isNotNull();
        assertThat(result.getProperty("script-directory").toString()).isEqualTo(scriptDirectory.getAbsolutePath());
    }

    //TODO: write up more tests


//    @Test
//    public void testGetEmptySubscriptions() throws Exception {
//
//        ResourceState result = client.read( new RequestContext.Builder().build(), "/admin/applications/testApp/resources/scripts");
//        result = client.read( new RequestContext.Builder().build(), "/testApp/mock");
//        //System.out.println("PROPERTIES : " + System.getProperties().keySet());
//        System.err.println("RESULT : " + result);
//
//        result.putProperty("Foo", "Bar");
//        client.update(new RequestContext.Builder().build(), "/testApp/mock", result);
//
//        result = client.read( new RequestContext.Builder().build(), "/testApp/mock");
//        System.err.println("RESULT2 : " + result);
//
//        ResourceState child = new DefaultResourceState("baz");
//        client.create(new RequestContext.Builder().build(),"/testApp/mock", child);
//        result = client.read( new RequestContext.Builder().build(), "/testApp/mock/baz");
//        System.err.println("CHILD : " + result);
//
//        ResourceState resource = new DefaultResourceState("testScript");
//        resource.putProperty("target-path", "/testApp/**");
//
//        client.create(new RequestContext.Builder().build(), "/admin/applications/testApp/resources/scripts/resource-triggered-scripts", resource );
//
//        Buffer buffer = readFile("hello.js");
//        //Buffer buffer = new Buffer("function preRead(request, libraries) { print('HELLO WORLD'); }");
//
//
//
//
//        ResourceState binaryResourceState = new JavaScriptResourceState(buffer.getByteBuf());
//        ResourceState binResult = client.create(new RequestContext.Builder().build(), "/admin/applications/testApp/resources/scripts/resource-triggered-scripts/testScript", binaryResourceState);
//
//        ResourceState reRead = client.read(new RequestContext.Builder().build(), "/testApp/mock");
//
//
//        ClassLoader cl = ClassLoader.getSystemClassLoader();
//
//        URL[] urls = ((URLClassLoader)cl).getURLs();
//
//        for(URL url: urls){
//            System.out.println(url.getFile());
//        }
//    }
}
