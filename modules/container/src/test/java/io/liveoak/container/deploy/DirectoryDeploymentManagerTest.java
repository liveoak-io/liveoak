package io.liveoak.container.deploy;

import java.io.File;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.state.ResourceState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class DirectoryDeploymentManagerTest {

    private File projectRoot;
    private LiveOakSystem system;
    private Client client;

    @Before
    public void setupUserDir() {
        String name = getClass().getName().replace(".", "/") + ".class";
        URL resource = getClass().getClassLoader().getResource(name);

        if (resource != null) {
            File current = new File(resource.getFile());

            while (current.exists()) {
                if (current.isDirectory()) {
                    if (new File(current, "pom.xml").exists()) {
                        this.projectRoot = current;
                        break;
                    }
                }

                current = current.getParentFile();
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        File etc = new File( this.projectRoot, "target/etc/resources" );
        if ( etc.exists() ) {
            File[] children = etc.listFiles();
            for ( File child : children ) {
                child.delete();
            }
        }
        this.system = LiveOakFactory.create(new File(this.projectRoot, "target/etc"));
        this.client = this.system.client();
    }

    @After
    public void tearDown() {
        this.system.stop();
    }


    @Test
    public void testDeployNewResource() throws Exception {
        MockConfigurableResource resource = new MockConfigurableResource("myresource");
        DefaultResourceState state = new DefaultResourceState();
        state.id( "myresource" );
        state.putProperty("type", "classpath");
        state.putProperty("class-name", MockConfigurableResource.class.getName());
        DefaultResourceState config = new DefaultResourceState();
        config.putProperty("name", "Bob");
        config.putProperty("age", 40);
        state.putProperty("config", config);

        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState result = this.client.create(requestContext, "/", state);
        assertThat( result ).isNotNull();

        File myresourceJson = new File( this.projectRoot, "target/etc/resources/myresource.json" );

        assertThat( myresourceJson ).exists();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode tree = mapper.readTree( myresourceJson );

        assertThat( tree ).isNotNull();

        assertThat( tree.get( "class-name" ).asText() ).isEqualTo( MockConfigurableResource.class.getName() );
        assertThat( tree.get( "type" ).asText() ).isEqualTo( "classpath" );

        JsonNode configTree = tree.get( "config" );

        assertThat( configTree ).isNotNull();
        assertThat( configTree.get( "name" ).asText() ).isEqualTo( "Bob" );
        assertThat( configTree.get( "age" ).asInt() ).isEqualTo( 40 );
    }

    @Test
    public void testUpdateConfig() throws Exception {
        MockConfigurableResource resource = new MockConfigurableResource("myresource");
        DefaultResourceState state = new DefaultResourceState();
        state.id( "myresource" );
        state.putProperty("type", "classpath");
        state.putProperty("class-name", MockConfigurableResource.class.getName());
        DefaultResourceState config = new DefaultResourceState();
        config.putProperty("name", "Bob");
        config.putProperty("age", 40);
        state.putProperty("config", config);

        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState result = this.client.create(requestContext, "/", state);
        assertThat( result ).isNotNull();

        File myresourceJson = new File( this.projectRoot, "target/etc/resources/myresource.json" );

        assertThat( myresourceJson ).exists();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode tree = mapper.readTree( myresourceJson );

        assertThat( tree ).isNotNull();

        assertThat( tree.get( "class-name" ).asText() ).isEqualTo(MockConfigurableResource.class.getName());
        assertThat( tree.get( "type" ).asText() ).isEqualTo( "classpath" );

        JsonNode configTree = tree.get( "config" );

        assertThat( configTree ).isNotNull();
        assertThat( configTree.get( "name" ).asText() ).isEqualTo("Bob");
        assertThat( configTree.get( "age" ).asInt() ).isEqualTo( 40 );

        config.putProperty("name", "Gary");
        config.putProperty( "age", 105 );

        result = this.client.update(requestContext, "/myresource;config", config);

        assertThat( result ).isNotNull();

        assertThat( myresourceJson ).exists();

        tree = mapper.readTree( myresourceJson );

        assertThat( tree ).isNotNull();

        assertThat( tree.get( "class-name" ).asText() ).isEqualTo( MockConfigurableResource.class.getName() );
        assertThat( tree.get( "type" ).asText() ).isEqualTo( "classpath" );

        configTree = tree.get( "config" );

        assertThat( configTree ).isNotNull();
        assertThat( configTree.get( "name" ).asText() ).isEqualTo( "Gary" );
        assertThat( configTree.get( "age" ).asInt() ).isEqualTo( 105 );
    }
}
