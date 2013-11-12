package org.projectodd.restafari.container;

import org.junit.Before;
import org.junit.Test;
import org.projectodd.restafari.container.codec.DefaultResourceState;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.List;
import java.util.stream.Collectors;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class NewDirectConnectorTest {


    private DefaultContainer container;
    private NewDirectConnector connector;


    @Before
    public void setUp() throws Exception {
        this.container = new DefaultContainer();
        this.connector = new NewDirectConnector(this.container);

        InMemoryDBResource db = new InMemoryDBResource("db");
        db.addMember(new InMemoryCollectionResource(db, "people"));
        db.addMember(new InMemoryCollectionResource(db, "dogs"));

        this.container.registerResource(db, new SimpleConfig());
    }

    @Test
    public void testRead() throws Throwable {

        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState result = this.connector.read(requestContext, "/");
        assertThat(result).isNotNull();

        List<ResourceState> members = result.members();

        assertThat(members).isNotEmpty();

        ResourceState db = members.stream().filter((e) -> e.id().equals("db")).findFirst().get();
        assertThat(db).isNotNull();

        ResourceState people = db.members().stream().filter((e) -> e.id().equals("people")).findFirst().get();
        assertThat(people).isNotNull();

        ResourceState dogs = db.members().stream().filter((e) -> e.id().equals("dogs")).findFirst().get();
        assertThat(dogs).isNotNull();
    }

    @Test
    public void testCreate() throws Throwable {

        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState bob = new DefaultResourceState( "bob" );
        bob.putProperty("name", "Bob McWhirter");

        ResourceState result = this.connector.create( requestContext, "/db/people", bob );

        assertThat( result ).isNotNull();
        assertThat( result.getProperty( "name" ) ).isEqualTo( "Bob McWhirter" );

        ResourceState people = this.connector.read( requestContext, "/db/people" );

        assertThat( people ).isNotNull();

        ResourceState foundBob = people.members().stream().filter( (e)->e.id().equals("bob")).findFirst().get();
        assertThat( foundBob ).isNotNull();
        assertThat( foundBob.getProperty( "name" )).isEqualTo( "Bob McWhirter" );

        foundBob = this.connector.read( requestContext, "/db/people/bob" );

        assertThat( foundBob ).isNotNull();
        assertThat( foundBob.getProperty( "name" )).isEqualTo("Bob McWhirter");
    }

    @Test
    public void testUpdate() throws Throwable {
        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState bob = new DefaultResourceState( "bob" );
        bob.putProperty( "name", "Bob McWhirter");

        ResourceState result = this.connector.create( requestContext, "/db/people", bob );

        assertThat( result ).isNotNull();
        assertThat( result.getProperty( "name" ) ).isEqualTo( "Bob McWhirter" );

        ResourceState foundBob = this.connector.read( requestContext, "/db/people/bob" );
        assertThat( foundBob ).isNotNull();
        assertThat( foundBob.getProperty( "name" )).isEqualTo( "Bob McWhirter" );

        bob = new DefaultResourceState( "bob" );
        bob.putProperty( "name", "Robert McWhirter");

        result = this.connector.update( requestContext, "/db/people/bob", bob );
        assertThat( result ).isNotNull();
        assertThat( result.getProperty( "name" )).isEqualTo( "Robert McWhirter" );

        foundBob = this.connector.read( requestContext, "/db/people/bob" );
        assertThat( foundBob ).isNotNull();
        assertThat( foundBob.getProperty( "name" )).isEqualTo( "Robert McWhirter" );


    }

}
