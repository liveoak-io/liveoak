package io.liveoak.container.codec.state;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import io.liveoak.container.InMemoryObjectResource;
import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.container.codec.driver.EncodingDriver;
import io.liveoak.container.codec.driver.RootEncodingDriver;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ResourceStateEncoderTest {

    protected EncodingDriver createDriver(Resource resource, CompletableFuture<ResourceState> future) throws Exception {
        ResourceStateEncoder encoder = new ResourceStateEncoder();
        ByteBuf buffer = Unpooled.buffer();
        encoder.initialize(buffer);
        RootEncodingDriver driver = new RootEncodingDriver(new RequestContext.Builder().build(), encoder, resource, () -> {
            future.complete(encoder.root());
        });
        return driver;
    }

    @Test
    public void testEmptyObject() throws Exception {
        DefaultResourceState state = new DefaultResourceState();
        InMemoryObjectResource resource = new InMemoryObjectResource(null, "bob", state);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(resource, future);
        driver.encode();

        ResourceState encoded = future.get();

        assertThat( encoded.id() ).isEqualTo("bob");
        assertThat( encoded.getPropertyNames() ).isEmpty();
        assertThat( encoded.members() ).isEmpty();
    }

    @Test
    public void testObjectWithProperties() throws Exception {
        DefaultResourceState state = new DefaultResourceState();
        state.putProperty("name", "Bob McWhirter");
        InMemoryObjectResource resource = new InMemoryObjectResource(null, "bob", state);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(resource, future);
        driver.encode();
        ResourceState encoded = future.get();

        assertThat( encoded.id() ).isEqualTo( "bob" );
        assertThat( encoded.getPropertyNames() ).hasSize(1);
        assertThat( encoded.getPropertyNames() ).contains("name");
        assertThat( encoded.getProperty( "name" )).isEqualTo( "Bob McWhirter" );
        assertThat( encoded.members() ).hasSize(0);
    }

    @Test
    public void testObjectWithResourceProperty() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState();
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");
        InMemoryObjectResource mosesResourse = new InMemoryObjectResource(null, "moses", mosesState);


        DefaultResourceState bobState = new DefaultResourceState();
        bobState.putProperty("name", "Bob McWhirter");
        bobState.putProperty("dog", mosesResourse);
        InMemoryObjectResource bobResource = new InMemoryObjectResource(null, "bob", bobState);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(bobResource, future);
        driver.encode();
        ResourceState encoded = future.get();

        System.err.println(encoded);

        assertThat( encoded.id() ).isEqualTo( "bob" );
        assertThat( encoded.getPropertyNames() ).hasSize( 2 );

        assertThat( encoded.getPropertyNames() ).contains( "name" );
        assertThat( encoded.getProperty( "name" )).isEqualTo( "Bob McWhirter" );

        assertThat( encoded.getPropertyNames() ).contains( "dog" );
        assertThat( encoded.getProperty("dog")).isNotNull();
        assertThat( encoded.getProperty( "dog" )).isInstanceOf(URI.class);

        assertThat( encoded.members() ).hasSize(0);

        URI encodedMoses = (URI) encoded.getProperty( "dog" );
        assertThat( encodedMoses ).isEqualTo( mosesResourse.uri() );
    }

    @Test
    public void testObjectWithResourceArrayProperty() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState();
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");
        InMemoryObjectResource mosesResourse = new InMemoryObjectResource(null, "moses", mosesState);

        DefaultResourceState onlyState = new DefaultResourceState();
        onlyState.putProperty("name", "Only");
        onlyState.putProperty("breed", "Lab/Huskie Mix");
        InMemoryObjectResource onlyResource = new InMemoryObjectResource(null, "only", onlyState);

        DefaultResourceState bobState = new DefaultResourceState();
        bobState.putProperty("name", "Bob McWhirter");
        ArrayList<Resource> dogs = new ArrayList<>();
        dogs.add(mosesResourse);
        dogs.add(onlyResource);
        bobState.putProperty("dogs", dogs);
        InMemoryObjectResource bobResource = new InMemoryObjectResource(null, "bob", bobState);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(bobResource, future);
        driver.encode();

        ResourceState encoded = future.get();
        System.err.println(encoded);

        assertThat( encoded.id() ).isEqualTo( "bob" );
        assertThat( encoded.getPropertyNames() ).hasSize( 2 );

        assertThat( encoded.getPropertyNames() ).contains( "name" );
        assertThat( encoded.getProperty( "name" )).isEqualTo( "Bob McWhirter" );

        assertThat( encoded.getPropertyNames() ).contains( "dogs" );
        assertThat( encoded.getProperty( "dogs" )).isNotNull();
        assertThat( encoded.getProperty( "dogs" )).isInstanceOf( List.class );

        assertThat( encoded.members() ).hasSize(0);

        List<URI> encodedDogs = (List<URI>) encoded.getProperty( "dogs" );

        assertThat( encodedDogs ).hasSize( 2 );

        assertThat( encodedDogs.get(0)).isEqualTo( mosesResourse.uri() );
        assertThat( encodedDogs.get(1)).isEqualTo(onlyResource.uri());
    }
}
