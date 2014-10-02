package io.liveoak.mongo.config;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.exceptions.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.fest.assertions.Fail;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoConfigReadPreferenceTest extends BaseMongoConfigTest {

    @Test
    public void testDefault() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testDefaultDB");
        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testDefaultDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(ReadPreferenceResource.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceResource.ID);
        // the default values
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TYPE.toString())).isEqualTo(ReadPreferenceResource.Types.PRIMARY.toString());
        assertThat(readPreferenceResourceState.getPropertyNames().size()).isEqualTo(1); //Since its a primary type, there cannot be tags
    }

    @Test
    public void testConfigureType() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testConfigureTypeDB");

        ResourceState configReadPref = new DefaultResourceState();
        configReadPref.putProperty(ReadPreferenceResource.Options.TYPE.toString(),"secondary"); // use new string value here, if using ReadPreferenceResource.Types.SECONDARY.toString() then '==' will incorrectly work when we should be using equals() 
        config.putProperty(ReadPreferenceResource.ID, configReadPref);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);


        assertThat(result.getProperty("db")).isEqualTo("testConfigureTypeDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(ReadPreferenceResource.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceResource.ID);
        assertThat(readPreferenceResourceState.getPropertyNames().size()).isEqualTo(2); // since not a primary type, will have a list of tags
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TYPE.toString())).isEqualTo(ReadPreferenceResource.Types.SECONDARY.toString());
        assertThat((List) readPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TAGS.toString())).isEmpty();
    }

    @Test
    public void testUpdateType() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testUpdateTypeDB");
        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);


        assertThat(result.getProperty("db")).isEqualTo("testUpdateTypeDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(ReadPreferenceResource.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceResource.ID);
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TYPE.toString())).isEqualTo(ReadPreferenceResource.Types.PRIMARY.toString());


        ResourceState updatedResourceState = new DefaultResourceState();
        updatedResourceState.putProperty(ReadPreferenceResource.Options.TYPE.toString(), ReadPreferenceResource.Types.NEAREST.toString());

        config = new DefaultResourceState();
        config.putProperty(ReadPreferenceResource.ID, updatedResourceState);

        result = client.update(new RequestContext.Builder().build(), ADMIN_PATH, config);
        ResourceState updatedReadPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceResource.ID);
        assertThat(updatedReadPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TYPE.toString())).isEqualTo(ReadPreferenceResource.Types.NEAREST.toString());

    }

    @Test
    public void testSetWithTags() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testConfigureWithTagsDB");

        ResourceState configReadPref = new DefaultResourceState();
        configReadPref.putProperty(ReadPreferenceResource.Options.TYPE.toString(), ReadPreferenceResource.Types.SECONDARY.toString());

        ResourceState tagsResourceState = new DefaultResourceState();
        tagsResourceState.putProperty("foo", "bar");
        tagsResourceState.putProperty("hello", "world");

        configReadPref.putProperty(ReadPreferenceResource.Options.TAGS.toString(), tagsResourceState);


        config.putProperty(ReadPreferenceResource.ID, configReadPref);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testConfigureWithTagsDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(ReadPreferenceResource.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceResource.ID);
        assertThat(readPreferenceResourceState.getPropertyNames().size()).isEqualTo(2);
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TYPE.toString())).isEqualTo(ReadPreferenceResource.Types.SECONDARY.toString());
        List<ResourceState> tagsStates = (List) readPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TAGS.toString());
        assertThat(tagsStates.size()).isEqualTo(2);
        assertThat(tagsStates.get(0).getProperty("foo")).isEqualTo("bar");
        assertThat(tagsStates.get(1).getProperty("hello")).isEqualTo("world");
    }

    @Test
    public void testInvalidType() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testInvalidTypeDB");

        ResourceState configReadPref = new DefaultResourceState();
        configReadPref.putProperty(ReadPreferenceResource.Options.TYPE.toString(), "foobar");
        config.putProperty(ReadPreferenceResource.ID, configReadPref);

        try {
            setUpSystem(config);
            Fail.fail();
        } catch (InitializationException e) {
            //expected
        }
    }

    @Test
    public void testNullType() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testNullTypeDB");

        ResourceState configReadPref = new DefaultResourceState();
        configReadPref.putProperty(ReadPreferenceResource.Options.TYPE.toString(), null);
        config.putProperty(ReadPreferenceResource.ID, configReadPref);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testNullTypeDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(ReadPreferenceResource.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceResource.ID);
        // the default values
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TYPE.toString())).isEqualTo(ReadPreferenceResource.Types.PRIMARY.toString());
        assertThat(readPreferenceResourceState.getPropertyNames().size()).isEqualTo(1); //Since its a primary type, there cannot be tags
    }

    @Test
    public void testUpdateTags() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testUpdateTagsDB");

        ResourceState configReadPref = new DefaultResourceState();
        configReadPref.putProperty(ReadPreferenceResource.Options.TYPE.toString(), ReadPreferenceResource.Types.SECONDARY.toString());

        ResourceState tagsResourceState = new DefaultResourceState();
        tagsResourceState.putProperty("hello", "world");
        tagsResourceState.putProperty("loc", "east");

        configReadPref.putProperty(ReadPreferenceResource.Options.TAGS.toString(), tagsResourceState);
        config.putProperty(ReadPreferenceResource.ID, configReadPref);

        setUpSystem(config);

        ResourceState updatedConfig = new DefaultResourceState();
        ResourceState updatedConfigReadPref = new DefaultResourceState();
        ResourceState updatedTagConfig = new DefaultResourceState();
        updatedTagConfig.putProperty("loc", "west");
        updatedConfigReadPref.putProperty(ReadPreferenceResource.Options.TAGS.toString(), updatedTagConfig);

        updatedConfig.putProperty(ReadPreferenceResource.ID, updatedConfigReadPref);

        ResourceState result = client.update(new RequestContext.Builder().build(), ADMIN_PATH, updatedConfig);

        assertThat(result.getProperty("db")).isEqualTo("testUpdateTagsDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(ReadPreferenceResource.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceResource.ID);
        assertThat(readPreferenceResourceState.getPropertyNames().size()).isEqualTo(2);
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TYPE.toString())).isEqualTo(ReadPreferenceResource.Types.SECONDARY.toString());
        List<ResourceState> tagsStates = (List) readPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TAGS.toString());
        assertThat(tagsStates.size()).isEqualTo(1);
        assertThat(tagsStates.get(0).getProperty("loc")).isEqualTo("west");
    }

    @Test
    public void testRemoveEmptyTags() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testRemoveTagDB");

        ResourceState configReadPref = new DefaultResourceState();
        configReadPref.putProperty(ReadPreferenceResource.Options.TYPE.toString(), ReadPreferenceResource.Types.SECONDARY.toString());

        ResourceState tagsResourceState = new DefaultResourceState();
        tagsResourceState.putProperty("hello", "world");
        tagsResourceState.putProperty("loc", "east");

        configReadPref.putProperty(ReadPreferenceResource.Options.TAGS.toString(), tagsResourceState);
        config.putProperty(ReadPreferenceResource.ID, configReadPref);

        setUpSystem(config);

        ResourceState updatedConfig = new DefaultResourceState();
        ResourceState updatedConfigReadPref = new DefaultResourceState();
        ResourceState updatedTagConfig = new DefaultResourceState();

        updatedConfigReadPref.putProperty(ReadPreferenceResource.Options.TAGS.toString(), new DefaultResourceState());
        updatedConfig.putProperty(ReadPreferenceResource.ID, updatedConfigReadPref);

        ResourceState result = client.update(new RequestContext.Builder().build(), ADMIN_PATH, updatedConfig);

        assertThat(result.getProperty("db")).isEqualTo("testRemoveTagDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(ReadPreferenceResource.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceResource.ID);
        assertThat(readPreferenceResourceState.getPropertyNames().size()).isEqualTo(2);
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TYPE.toString())).isEqualTo(ReadPreferenceResource.Types.SECONDARY.toString());
        List<ResourceState> tagsStates = (List) readPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TAGS.toString());
        assertThat(tagsStates.size()).isEqualTo(0);
    }

    @Test
    public void testRemoveNullTags() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testRemoveTagDB");

        ResourceState configReadPref = new DefaultResourceState();
        configReadPref.putProperty(ReadPreferenceResource.Options.TYPE.toString(), ReadPreferenceResource.Types.SECONDARY.toString());

        ResourceState tagsResourceState = new DefaultResourceState();
        tagsResourceState.putProperty("hello", "world");
        tagsResourceState.putProperty("loc", "east");

        configReadPref.putProperty(ReadPreferenceResource.Options.TAGS.toString(), tagsResourceState);
        config.putProperty(ReadPreferenceResource.ID, configReadPref);

        setUpSystem(config);

        ResourceState updatedConfig = new DefaultResourceState();
        ResourceState updatedConfigReadPref = new DefaultResourceState();

        updatedConfigReadPref.putProperty(ReadPreferenceResource.Options.TAGS.toString(), null);
        updatedConfig.putProperty(ReadPreferenceResource.ID, updatedConfigReadPref);

        ResourceState result = client.update(new RequestContext.Builder().build(), ADMIN_PATH, updatedConfig);

        assertThat(result.getProperty("db")).isEqualTo("testRemoveTagDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(ReadPreferenceResource.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceResource.ID);
        assertThat(readPreferenceResourceState.getPropertyNames().size()).isEqualTo(2);
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TYPE.toString())).isEqualTo(ReadPreferenceResource.Types.SECONDARY.toString());
        List<ResourceState> tagsStates = (List) readPreferenceResourceState.getProperty(ReadPreferenceResource.Options.TAGS.toString());
        assertThat(tagsStates.size()).isEqualTo(0);
    }

}
