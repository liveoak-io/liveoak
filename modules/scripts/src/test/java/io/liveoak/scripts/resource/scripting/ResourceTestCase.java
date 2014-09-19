package io.liveoak.scripts.resource.scripting;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ResourceTestCase extends BaseScriptingTestCase {
//
//    @Test
//    public void testAddingProperties() throws Exception {
//        //create the metadata
//        ResourceState resourceState = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, createScriptMetaData("addProperty", "/testApp/mock"));
//        assertThat(resourceState).isNotNull();
//        assertThat(resourceState.id()).isEqualTo("addProperty");
//
//        //upload the javascript file
//        ResourceState binaryResourceState = new JavaScriptResourceState(readFile("addProperty.js"));
//        ResourceState javascriptState = client.create(new RequestContext.Builder().build(), resourceState.uri().toString(), binaryResourceState);
//        assertThat(javascriptState).isNotNull();
//
//        JsonNode postObject = ObjectMapperFactory.create().readTree("{'bar' : 'BAR' }");
//        postHttpResource("http://localhost:8080/testApp/mock", postObject);
//
//
//
//    }
}
