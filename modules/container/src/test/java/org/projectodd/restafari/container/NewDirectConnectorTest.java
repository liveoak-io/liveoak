package org.projectodd.restafari.container;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.restafari.container.codec.DefaultObjectResourceState;
import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.ReturnFields;
import org.projectodd.restafari.spi.resource.async.PropertyResource;
import org.projectodd.restafari.spi.state.CollectionResourceState;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collector;
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

        assertThat(result).isInstanceOf(CollectionResourceState.class);

        CollectionResourceState collectionState = (CollectionResourceState) result;

        List<ResourceState> members = collectionState.members().collect(Collectors.toList());

        assertThat(members).isNotEmpty();

        CollectionResourceState db = (CollectionResourceState) collectionState.members().filter((e) -> e.id().equals("db")).findFirst().get();
        assertThat(db).isNotNull();

        CollectionResourceState people = (CollectionResourceState) db.members().filter((e) -> e.id().equals("people")).findFirst().get();
        assertThat(people).isNotNull();

        CollectionResourceState dogs = (CollectionResourceState) db.members().filter((e) -> e.id().equals("dogs")).findFirst().get();
        assertThat(dogs).isNotNull();
    }

    @Test
    public void testCreate() throws Throwable {

        RequestContext requestContext = new RequestContext.Builder().build();
        ObjectResourceState bob = new DefaultObjectResourceState( "bob" );
        bob.addProperty( "name", "Bob McWhirter");

        ObjectResourceState result = (ObjectResourceState) this.connector.create( requestContext, "/db/people", bob );

        assertThat( result ).isNotNull();
        assertThat( result.getProperty( "name" ) ).isEqualTo( "Bob McWhirter" );

        CollectionResourceState people = (CollectionResourceState) this.connector.read( requestContext, "/db/people" );

        assertThat( people ).isNotNull();

        ObjectResourceState foundBob = (ObjectResourceState) people.members().filter( (e)->e.id().equals("bob")).findFirst().get();
        assertThat( foundBob ).isNotNull();
        assertThat( foundBob.getProperty( "name" )).isEqualTo( "Bob McWhirter" );

        foundBob = (ObjectResourceState) this.connector.read( requestContext, "/db/people/bob" );

        assertThat( foundBob ).isNotNull();
        assertThat( foundBob.getProperty( "name" )).isEqualTo("Bob McWhirter");
    }

    @Test
    public void testUpdate() throws Throwable {
        RequestContext requestContext = new RequestContext.Builder().build();
        ObjectResourceState bob = new DefaultObjectResourceState( "bob" );
        bob.addProperty( "name", "Bob McWhirter");

        ObjectResourceState result = (ObjectResourceState) this.connector.create( requestContext, "/db/people", bob );

        assertThat( result ).isNotNull();
        assertThat( result.getProperty( "name" ) ).isEqualTo( "Bob McWhirter" );

        ObjectResourceState foundBob = (ObjectResourceState) this.connector.read( requestContext, "/db/people/bob" );
        assertThat( foundBob ).isNotNull();
        assertThat( foundBob.getProperty( "name" )).isEqualTo( "Bob McWhirter" );

        bob = new DefaultObjectResourceState( "bob" );
        bob.addProperty( "name", "Robert McWhirter");

        result = (ObjectResourceState) this.connector.update( requestContext, "/db/people/bob", bob );
        assertThat( result ).isNotNull();
        assertThat( result.getProperty( "name" )).isEqualTo( "Robert McWhirter" );

        foundBob = (ObjectResourceState) this.connector.read( requestContext, "/db/people/bob" );
        assertThat( foundBob ).isNotNull();
        assertThat( foundBob.getProperty( "name" )).isEqualTo( "Robert McWhirter" );


    }

}
