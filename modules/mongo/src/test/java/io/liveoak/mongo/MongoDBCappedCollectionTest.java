package io.liveoak.mongo;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.DeleteNotSupportedException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.fest.assertions.Fail;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBCappedCollectionTest extends BaseMongoDBTest {

    @Test
    public void testCreateCappedCollection() throws Exception {
        // check that we can create the resource
        ResourceState state = new DefaultResourceState("lastFive");
        state.putProperty("capped", "true");
        state.putProperty("size", 1024);
        state.putProperty("max", 5);

        ResourceState createdResource = client.create(new RequestContext.Builder().build(), "/testApp/storage", state);
        assertThat(createdResource).isNotNull();
        assertThat(createdResource.id()).isEqualTo("lastFive");

        // test that we get this resource back on a read
        ResourceState lastFive = client.read(new RequestContext.Builder().build(), "/testApp/storage/lastFive");
        assertThat(lastFive).isNotNull();
        assertThat(lastFive.id()).isEqualTo( "lastFive" );
        assertThat(lastFive.getProperty( "capped" )).isEqualTo( true );
        assertThat(lastFive.getProperty("max")).isEqualTo(5);
        assertThat( lastFive.members().size() ).isEqualTo( 0 );
    }

    @Test
    public void testWriteCappedCollection() throws Exception {
        // check that we can create the resource
        ResourceState state = new DefaultResourceState("lastTwo");
        state.putProperty("capped", "true");
        state.putProperty("size", 1024);
        state.putProperty("max", 2);

        ResourceState createdResource = client.create(new RequestContext.Builder().build(), "/testApp/storage", state);
        assertThat(createdResource).isNotNull();
        assertThat(createdResource.id()).isEqualTo("lastTwo");

        client.create(new RequestContext.Builder().build(), "/testApp/storage/lastTwo", new DefaultResourceState( "first" ));
        client.create(new RequestContext.Builder().build(), "/testApp/storage/lastTwo", new DefaultResourceState( "second" ));
        // since the capped collection only holds 2 items, this should remove the first element:
        client.create(new RequestContext.Builder().build(), "/testApp/storage/lastTwo", new DefaultResourceState( "third" ));

        // test that we get this resource back on a read
        ResourceState lastTwo = client.read(new RequestContext.Builder().build(), "/testApp/storage/lastTwo");
        assertThat(lastTwo).isNotNull();
        assertThat(lastTwo.id()).isEqualTo( "lastTwo" );
        assertThat(lastTwo.getProperty("capped")).isEqualTo( true );
        assertThat(lastTwo.getProperty("max")).isEqualTo(2);
        assertThat(lastTwo.members().size()).isEqualTo(2);
        assertThat(lastTwo.members().get(0).id()).isEqualTo("second");
        assertThat(lastTwo.members().get(1).id()).isEqualTo("third");
    }

    @Test
    public void testDeleteElementCappedCollection() throws Exception {
        // check that we can create the resource
        ResourceState state = new DefaultResourceState("onlyThree");
        state.putProperty("capped", "true");
        state.putProperty("size", 1024);
        state.putProperty("max", 3);

        ResourceState createdResource = client.create(new RequestContext.Builder().build(), "/testApp/storage", state);
        assertThat(createdResource).isNotNull();
        assertThat(createdResource.id()).isEqualTo("onlyThree");

        client.create(new RequestContext.Builder().build(), "/testApp/storage/onlyThree", new DefaultResourceState( "first" ));

        // this should fail since you cannot delete from a capped collection
        try {
            client.delete(new RequestContext.Builder().build(), "/testApp/storage/onlyThree/first");
            Fail.fail();
        } catch (DeleteNotSupportedException dnse) {
            //expected
        }

    }

    @Test
    public void testDeleteCappedCollection() throws Exception {
        // check that we can create the resource
        ResourceState state = new DefaultResourceState("testDelete");
        state.putProperty("capped", "true");
        state.putProperty("size", 1024);
        state.putProperty("max", 3);

        ResourceState createdResource = client.create(new RequestContext.Builder().build(), "/testApp/storage", state);
        assertThat(createdResource).isNotNull();
        assertThat(createdResource.id()).isEqualTo("testDelete");

        ResourceState deleteResource = client.delete( new RequestContext.Builder().build(), "/testApp/storage/testDelete" );
        assertThat(deleteResource).isNotNull();
        assertThat(deleteResource.id()).isEqualTo("testDelete");

    }
}
