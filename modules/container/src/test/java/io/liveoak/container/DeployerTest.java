package io.liveoak.container;

import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class DeployerTest {

    private DefaultContainer container;
    private DirectConnector connector;

    @Before
    public void setUp() throws Exception {
        this.container = new DefaultContainer();
        this.connector = new DirectConnector(this.container);

        //InMemoryDBResource db = new InMemoryDBResource("db");
        //db.addMember(new InMemoryCollectionResource(db, "people"));
        //db.addMember(new InMemoryCollectionResource(db, "dogs"));
//
        //this.container.registerResource(db, new SimpleConfig());
    }

    @Test
    public void testDeployNewResource() throws Exception {

        RequestContext context = new RequestContext.Builder().build();
        ResourceState state = new DefaultResourceState();

        state.id( "memory" );
        state.putProperty( "type", "classpath");
        state.putProperty( "class-name", InMemoryDBResource.class.getName() );

        ResourceState result = this.connector.create(context, "/", state);

        assertThat( result ).isNotNull();
        assertThat( result.id() ).isEqualTo( "memory" );

        // attempt to use it

        state = new DefaultResourceState();
        state.putProperty( "name", "bob" );

        result = this.connector.create( context, "/memory", state );

        assertThat( result ).isNotNull();
        assertThat( result.getProperty( "name" ) ).isEqualTo( "bob" );

        result = this.connector.read( context, "/memory/" + result.id() );

        assertThat( result ).isNotNull();
        assertThat( result.getProperty( "name" ) ).isEqualTo( "bob" );
    }
}
