package io.liveoak.mongo.config;

import java.util.List;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.InitializationException;
import io.liveoak.spi.state.ResourceState;
import org.fest.assertions.Fail;
import org.junit.Test;

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

        assertThat(result.getProperty(ReadPreferenceState.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceState.ID);
        // the default values
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceState.TYPE)).isEqualTo(ReadPreferenceState.PRIMARY);
        assertThat(readPreferenceResourceState.getPropertyNames().size()).isEqualTo(1); //Since its a primary type, there cannot be tags
    }

    @Test
    public void testConfigureType() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testConfigureTypeDB");

        ResourceState configReadPref = new DefaultResourceState();
        configReadPref.putProperty(ReadPreferenceState.TYPE,"secondary"); // use new string value here, if using ReadPreferenceResource.Types.SECONDARY.toString() then '==' will incorrectly work when we should be using equals()
        config.putProperty(ReadPreferenceState.ID, configReadPref);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);


        assertThat(result.getProperty("db")).isEqualTo("testConfigureTypeDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(ReadPreferenceState.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceState.ID);
        assertThat(readPreferenceResourceState.getPropertyNames().size()).isEqualTo(2); // since not a primary type, will have a list of tags
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceState.TYPE)).isEqualTo(ReadPreferenceState.SECONDARY);
        assertThat((List) readPreferenceResourceState.getProperty(ReadPreferenceState.TAGS)).isEmpty();
    }

    @Test
    public void testUpdateType() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testUpdateTypeDB");
        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);


        assertThat(result.getProperty("db")).isEqualTo("testUpdateTypeDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(ReadPreferenceState.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceState.ID);
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceState.TYPE)).isEqualTo(ReadPreferenceState.PRIMARY);


        ResourceState updatedResourceState = new DefaultResourceState();
        updatedResourceState.putProperty(ReadPreferenceState.TYPE, ReadPreferenceState.NEAREST);

        config = new DefaultResourceState();
        config.putProperty("db", "testUpdateTypeDB");
        config.putProperty(ReadPreferenceState.ID, updatedResourceState);

        result = client.update(new RequestContext.Builder().build(), ADMIN_PATH, config);
        ResourceState updatedReadPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceState.ID);
        assertThat(updatedReadPreferenceResourceState.getProperty(ReadPreferenceState.TYPE)).isEqualTo(ReadPreferenceState.NEAREST);

    }

    @Test
    public void testSetWithTags() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testConfigureWithTagsDB");

        ResourceState configReadPref = new DefaultResourceState();
        configReadPref.putProperty(ReadPreferenceState.TYPE, ReadPreferenceState.SECONDARY);

        ResourceState tagsResourceState = new DefaultResourceState();
        tagsResourceState.putProperty("foo", "bar");
        tagsResourceState.putProperty("hello", "world");

        configReadPref.putProperty(ReadPreferenceState.TAGS, tagsResourceState);


        config.putProperty(ReadPreferenceState.ID, configReadPref);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testConfigureWithTagsDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(ReadPreferenceState.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceState.ID);
        assertThat(readPreferenceResourceState.getPropertyNames().size()).isEqualTo(2);
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceState.TYPE)).isEqualTo(ReadPreferenceState.SECONDARY);
        List<ResourceState> tagsStates = (List) readPreferenceResourceState.getProperty(ReadPreferenceState.TAGS);
        assertThat(tagsStates.size()).isEqualTo(2);
        assertThat(tagsStates.get(0).getProperty("foo")).isEqualTo("bar");
        assertThat(tagsStates.get(1).getProperty("hello")).isEqualTo("world");
    }

    @Test
    public void testInvalidType() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testInvalidTypeDB");

        ResourceState configReadPref = new DefaultResourceState();
        configReadPref.putProperty(ReadPreferenceState.TYPE, "foobar");
        config.putProperty(ReadPreferenceState.ID, configReadPref);

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
        configReadPref.putProperty(ReadPreferenceState.TYPE, null);
        config.putProperty(ReadPreferenceState.ID, configReadPref);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testNullTypeDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(ReadPreferenceState.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceState.ID);
        // the default values
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceState.TYPE)).isEqualTo(ReadPreferenceState.PRIMARY);
        assertThat(readPreferenceResourceState.getPropertyNames().size()).isEqualTo(1); //Since its a primary type, there cannot be tags
    }

    @Test
    public void testUpdateTags() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testUpdateTagsDB");

        ResourceState configReadPref = new DefaultResourceState();
        configReadPref.putProperty(ReadPreferenceState.TYPE, ReadPreferenceState.SECONDARY);

        ResourceState tagsResourceState = new DefaultResourceState();
        tagsResourceState.putProperty("hello", "world");
        tagsResourceState.putProperty("loc", "east");

        configReadPref.putProperty(ReadPreferenceState.TAGS, tagsResourceState);
        config.putProperty(ReadPreferenceState.ID, configReadPref);

        setUpSystem(config);

        ResourceState updatedConfig = new DefaultResourceState();
        updatedConfig.putProperty("db", "testUpdateTagsDB");
        ResourceState updatedConfigReadPref = new DefaultResourceState();
        updatedConfigReadPref.putProperty(ReadPreferenceState.TYPE, ReadPreferenceState.SECONDARY_PREFERRED);
        ResourceState updatedTagConfig = new DefaultResourceState();
        updatedTagConfig.putProperty("loc", "west");
        updatedConfigReadPref.putProperty(ReadPreferenceState.TAGS, updatedTagConfig);

        updatedConfig.putProperty(ReadPreferenceState.ID, updatedConfigReadPref);
        ResourceState result = client.update(new RequestContext.Builder().build(), ADMIN_PATH, updatedConfig);

        assertThat(result.getProperty("db")).isEqualTo("testUpdateTagsDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(ReadPreferenceState.ID)).isNotNull();
        ResourceState readPreferenceResourceState = (ResourceState) result.getProperty(ReadPreferenceState.ID);
        assertThat(readPreferenceResourceState.getPropertyNames().size()).isEqualTo(2);
        assertThat(readPreferenceResourceState.getProperty(ReadPreferenceState.TYPE)).isEqualTo(ReadPreferenceState.SECONDARY_PREFERRED);
        List<ResourceState> tagsStates = (List) readPreferenceResourceState.getProperty(ReadPreferenceState.TAGS);
        assertThat(tagsStates.size()).isEqualTo(1);
        assertThat(tagsStates.get(0).getProperty("loc")).isEqualTo("west");
    }
}
