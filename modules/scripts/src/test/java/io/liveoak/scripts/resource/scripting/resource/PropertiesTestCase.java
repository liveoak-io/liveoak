package io.liveoak.scripts.resource.scripting.resource;

import com.fasterxml.jackson.databind.JsonNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.scripts.JavaScriptResourceState;
import io.liveoak.scripts.resource.scripting.BaseScriptingTestCase;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class PropertiesTestCase extends BaseScriptingTestCase {

    @Before
    public void setupTests() throws Exception {
        //check that there are no other scripts configured
        ResourceState initialState = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH);
        assertThat(initialState.members()).isEmpty();

        //create the metadata for the script
        ResourceState resourceState = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("propertiesTest", "/testApp/mock/*").build());
        assertThat(resourceState).isNotNull();
        assertThat(resourceState.id()).isEqualTo("propertiesTest");

        //upload the javascript file
        ResourceState binaryResourceState = new JavaScriptResourceState(readFile("testProperty.js"));
        ResourceState javascriptState = client.create(new RequestContext.Builder().build(), resourceState.uri().toString(), binaryResourceState);
        assertThat(javascriptState).isNotNull();
    }

    @Test
    public void testPropertiesCreate() throws Exception {
        JsonNode postObject = ObjectMapperFactory.create().readTree("{'cat' : 'Charles', 'rabbit' : 'Richard', 'urchin' : 'Uriel', 'dog' : 'Danielle' }");
        JsonNode jsonResult = createResource("http://localhost:8080/testApp/mock", postObject);


        // Read the values actually stored, eg what would have been modified by the preCreate method
        ResourceState clientResult = client.read(new RequestContext.Builder().build(), "/testApp/mock/" + jsonResult.get("id").textValue());
        assertThat(clientResult.getProperty("cat")).isEqualTo("Danielle");
        assertThat(clientResult.getProperty("rabbit")).isNull();
        assertThat(clientResult.getProperty("urchin")).isEqualTo("Uriel");
        assertThat(clientResult.getProperty("dog")).isEqualTo("Danielle");
        assertThat(clientResult.getProperty("parrot")).isEqualTo("Pete");


        // Check the returned value for what would have been done by the postCreate method
        assertThat(jsonResult).isNotNull();
        assertThat(jsonResult.get("cat").textValue()).isEqualTo("Danielle");
        assertThat(jsonResult.get("rabbit").textValue()).isEqualTo("Pete");
        assertThat(jsonResult.get("urchin").textValue()).isEqualTo("Uriel");
        assertThat(jsonResult.get("dog")).isNull();
        assertThat(jsonResult.get("parrot").textValue()).isEqualTo("Pete");
        assertThat(jsonResult.get("pig").textValue()).isEqualTo("Percy");
    }

    @Test
    public void testPropertiesRead() throws Exception {
        JsonNode postObject = ObjectMapperFactory.create().readTree("{'cat' : 'Charles', 'rabbit' : 'Richard', 'urchin' : 'Uriel', 'dog' : 'Danielle' }");
        ResourceState resource = client.create(new RequestContext.Builder().build(), "/testApp/mock", ConversionUtils.convert(postObject));

        JsonNode result = getResourceAsJson("http://localhost:8080/testApp/mock/" + resource.id());

        assertThat(result).isNotNull();
        assertThat(result.get("cat").textValue()).isEqualTo("Charles");
        assertThat(result.get("rabbit").textValue()).isEqualTo("Charles");
        assertThat(result.get("urchin")).isNull();
        assertThat(result.get("dog").textValue()).isEqualTo("Danielle");
        assertThat(result.get("porcupine").textValue()).isEqualTo("Porky");
    }

    @Test
    public void testPropertiesUpdate() throws Exception {
        //create the object using the client
        JsonNode postObject = ObjectMapperFactory.create().readTree("{'cat' : 'Charles', 'rabbit' : 'Richard', 'urchin' : 'Uriel', 'dog' : 'Danielle' }");
        ResourceState resource = client.create(new RequestContext.Builder().build(), "/testApp/mock", ConversionUtils.convert(postObject));

        JsonNode putObject = ObjectMapperFactory.create().readTree("{'cat' : 'Cleo', 'rabbit' : 'Rex', 'urchin' : 'Urkel', 'dog' : 'Dan' }");
        JsonNode jsonResult = updateResource("http://localhost:8080/testApp/mock/" + resource.id(), putObject);

        // Read the values actually stored, eg what would have been modified by the preUpdate method
        ResourceState clientResult = client.read(new RequestContext.Builder().build(), "/testApp/mock/" + resource.id());
        assertThat(clientResult.getProperty("cat")).isEqualTo("Claude");
        assertThat(clientResult.getProperty("urchin")).isEqualTo("Urkel");
        assertThat(clientResult.getProperty("dog")).isEqualTo("Rex");
        assertThat(clientResult.getProperty("penguin")).isEqualTo("Pat");
        assertThat(clientResult.getProperty("rabbit")).isNull();


        // Check the returned value for what would have been done by the postUpdate method
        assertThat(jsonResult).isNotNull();
        assertThat(jsonResult.get("cat").textValue()).isEqualTo("Rex");
        assertThat(jsonResult.get("rabbit")).isNull();
        assertThat(jsonResult.get("urchin").textValue()).isEqualTo("Urkel");
        assertThat(jsonResult.get("dog")).isNull();
        assertThat(jsonResult.get("penguin").textValue()).isEqualTo("Pat");
        assertThat(jsonResult.get("platypus").textValue()).isEqualTo("Patricia");

    }

    @Test
    public void testPropertiesDelete() throws Exception {
        //create the object using the client
        JsonNode postObject = ObjectMapperFactory.create().readTree("{'cat' : 'Charles', 'rabbit' : 'Richard', 'urchin' : 'Uriel', 'dog' : 'Danielle' }");
        ResourceState resource = client.create(new RequestContext.Builder().build(), "/testApp/mock", ConversionUtils.convert(postObject));

        JsonNode jsonResult = deleteResourceAsJson("http://localhost:8080/testApp/mock/" + resource.id());

        // Check the returned value for what would have been done by the postDelete method
        assertThat(jsonResult).isNotNull();
        assertThat(jsonResult.get("cat")).isNull();
        assertThat(jsonResult.get("rabbit").textValue()).isEqualTo("Richard");
        assertThat(jsonResult.get("urchin").textValue()).isEqualTo("Uriel");
        assertThat(jsonResult.get("dog").textValue()).isEqualTo("Charles");
        assertThat(jsonResult.get("parrot").textValue()).isEqualTo("Polly");

    }
}
