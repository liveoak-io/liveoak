/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec;

import java.util.concurrent.CompletableFuture;

import io.liveoak.common.DefaultReturnFields;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.codec.driver.EncodingDriver;
import io.liveoak.common.codec.driver.RootEncodingDriver;
import io.liveoak.common.codec.state.ResourceStateEncoder;
import io.liveoak.container.InMemoryCollectionResource;
import io.liveoak.container.InMemoryObjectResource;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceRef;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class EncoderReturnFieldsTest {

    protected EncodingDriver createDriver(Resource resource, String fieldsSpec, CompletableFuture<ResourceState> future) throws Exception {
        ResourceStateEncoder encoder = new ResourceStateEncoder();
        ByteBuf buffer = Unpooled.buffer();
        encoder.initialize(buffer);
        ReturnFields returnFields = ReturnFields.ALL;
        if (fieldsSpec != null && !fieldsSpec.trim().equals("")) {
            returnFields = new DefaultReturnFields(fieldsSpec);
        }
        RequestContext requestContext = new RequestContext.Builder().returnFields(returnFields).build();
        RootEncodingDriver driver = new RootEncodingDriver(requestContext, encoder, resource, () -> {
            future.complete(encoder.root());
        }, null);
        return driver;
    }

    @Test
    public void testEmptyObjectAllFields() throws Exception {
        DefaultResourceState state = new DefaultResourceState();
        InMemoryObjectResource resource = new InMemoryObjectResource(null, "bob", state);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(resource, null, future);
        driver.encode();

        ResourceState encoded = future.get();

        assertThat(encoded.id()).isEqualTo("bob");
        assertThat(encoded.getPropertyNames()).isEmpty();
        assertThat(encoded.members()).isEmpty();
        assertThat(encoded.getPropertyNames()).hasSize(0);
    }

    @Test
    public void testEmptyObjectSomeFields() throws Exception {
        DefaultResourceState state = new DefaultResourceState();
        InMemoryObjectResource resource = new InMemoryObjectResource(null, "bob", state);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(resource, "dogs,cheese", future);
        driver.encode();

        ResourceState encoded = future.get();

        assertThat(encoded.id()).isEqualTo("bob");
        assertThat(encoded.getPropertyNames()).isEmpty();
        assertThat(encoded.members()).isEmpty();
        assertThat(encoded.getPropertyNames()).hasSize(0);
    }

    @Test
    public void testObjectWithPropertiesAllFields() throws Exception {
        DefaultResourceState state = new DefaultResourceState();
        state.putProperty("name", "Bob McWhirter");
        state.putProperty("shoe-size", 15);
        InMemoryObjectResource resource = new InMemoryObjectResource(null, "bob", state);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(resource, null, future);
        driver.encode();
        ResourceState encoded = future.get();

        assertThat(encoded.id()).isEqualTo("bob");
        assertThat(encoded.getPropertyNames()).hasSize(2);
        assertThat(encoded.getPropertyNames()).contains("name");
        assertThat(encoded.getProperty("name")).isEqualTo("Bob McWhirter");
        assertThat(encoded.getPropertyNames()).contains("shoe-size");
        assertThat(encoded.getProperty("shoe-size")).isEqualTo(15);
        assertThat(encoded.members()).hasSize(0);
    }

    @Test
    public void testObjectWithPropertiesSomeFields() throws Exception {
        DefaultResourceState state = new DefaultResourceState();
        state.putProperty("name", "Bob McWhirter");
        state.putProperty("shoe-size", 15);
        InMemoryObjectResource resource = new InMemoryObjectResource(null, "bob", state);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(resource, "name", future);
        driver.encode();
        ResourceState encoded = future.get();

        assertThat(encoded.id()).isEqualTo("bob");
        assertThat(encoded.getPropertyNames()).hasSize(1);
        assertThat(encoded.getPropertyNames()).contains("name");
        assertThat(encoded.getProperty("name")).isEqualTo("Bob McWhirter");
        assertThat(encoded.members()).hasSize(0);
    }

    @Test
    public void testObjectWithPropertiesSomeFieldsWhichHappenToBeAll() throws Exception {
        DefaultResourceState state = new DefaultResourceState();
        state.putProperty("name", "Bob McWhirter");
        state.putProperty("shoe-size", 15);
        InMemoryObjectResource resource = new InMemoryObjectResource(null, "bob", state);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(resource, "shoe-size,name", future);
        driver.encode();
        ResourceState encoded = future.get();

        assertThat(encoded.id()).isEqualTo("bob");
        assertThat(encoded.getPropertyNames()).hasSize(2);
        assertThat(encoded.getPropertyNames()).contains("name");
        assertThat(encoded.getProperty("name")).isEqualTo("Bob McWhirter");
        assertThat(encoded.getPropertyNames()).contains("shoe-size");
        assertThat(encoded.getProperty("shoe-size")).isEqualTo(15);
        assertThat(encoded.members()).hasSize(0);
    }

    @Test
    public void testObjectWithResourcePropertyIncludedButNotExpanded() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState();
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");
        InMemoryObjectResource mosesResourse = new InMemoryObjectResource(null, "moses", mosesState);


        DefaultResourceState bobState = new DefaultResourceState();
        bobState.putProperty("name", "Bob McWhirter");
        bobState.putProperty("dog", mosesResourse);
        InMemoryObjectResource bobResource = new InMemoryObjectResource(null, "bob", bobState);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(bobResource, null, future);
        driver.encode();
        ResourceState encoded = future.get();

        System.err.println(encoded);

        assertThat(encoded.id()).isEqualTo("bob");
        assertThat(encoded.getPropertyNames()).hasSize(2);

        assertThat(encoded.getPropertyNames()).contains("name");
        assertThat(encoded.getProperty("name")).isEqualTo("Bob McWhirter");

        assertThat(encoded.getPropertyNames()).contains("dog");
        assertThat(encoded.getProperty("dog")).isNotNull();
        assertThat(encoded.getProperty("dog")).isInstanceOf(ResourceRef.class);

        assertThat(encoded.members()).hasSize(0);

        ResourceRef mosesRef = (ResourceRef) encoded.getProperty("dog");
        assertThat(mosesRef.uri()).isEqualTo(mosesResourse.uri());
    }

    @Test
    public void testObjectWithResourcePropertyNotIncluded() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState();
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");
        InMemoryObjectResource mosesResourse = new InMemoryObjectResource(null, "moses", mosesState);


        DefaultResourceState bobState = new DefaultResourceState();
        bobState.putProperty("name", "Bob McWhirter");
        bobState.putProperty("dog", mosesResourse);
        InMemoryObjectResource bobResource = new InMemoryObjectResource(null, "bob", bobState);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(bobResource, "name", future);
        driver.encode();
        ResourceState encoded = future.get();

        System.err.println(encoded);

        assertThat(encoded.id()).isEqualTo("bob");
        assertThat(encoded.getPropertyNames()).hasSize(1);

        assertThat(encoded.getPropertyNames()).contains("name");
        assertThat(encoded.getProperty("name")).isEqualTo("Bob McWhirter");

        assertThat(encoded.members()).hasSize(0);

    }

    @Test
    public void testObjectWithResourcePropertyFullyExpanded() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState();
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");
        InMemoryObjectResource mosesResourse = new InMemoryObjectResource(null, "moses", mosesState);


        DefaultResourceState bobState = new DefaultResourceState();
        bobState.putProperty("name", "Bob McWhirter");
        bobState.putProperty("dog", mosesResourse);
        InMemoryObjectResource bobResource = new InMemoryObjectResource(null, "bob", bobState);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(bobResource, "name,dog(*)", future);
        driver.encode();
        ResourceState encoded = future.get();

        System.err.println(encoded);

        assertThat(encoded.id()).isEqualTo("bob");
        assertThat(encoded.getPropertyNames()).hasSize(2);

        assertThat(encoded.getPropertyNames()).contains("name");
        assertThat(encoded.getProperty("name")).isEqualTo("Bob McWhirter");

        assertThat(encoded.getPropertyNames()).contains("dog");
        assertThat(encoded.getProperty("dog")).isInstanceOf(ResourceState.class);

        assertThat(encoded.members()).hasSize(0);

    }

    @Test
    public void testObjectWithExclude() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState();
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");
        InMemoryObjectResource mosesResourse = new InMemoryObjectResource(null, "moses", mosesState);


        DefaultResourceState bobState = new DefaultResourceState();
        bobState.putProperty("first-name", "Bob");
        bobState.putProperty("last-name", "McWhirter");
        bobState.putProperty("dog", mosesResourse);
        InMemoryObjectResource bobResource = new InMemoryObjectResource(null, "bob", bobState);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(bobResource, "*,-last-name,dog(*,-name)", future);
        driver.encode();
        ResourceState encoded = future.get();

        assertThat(encoded.id()).isEqualTo("bob");
        assertThat(encoded.getPropertyNames()).hasSize(2);

        assertThat(encoded.getPropertyNames()).contains("first-name");
        assertThat(encoded.getProperty("first-name")).isEqualTo("Bob");
        assertThat(encoded.getProperty("last-name")).isNull();

        assertThat(encoded.getPropertyNames()).contains("dog");
        assertThat(encoded.getProperty("dog")).isInstanceOf(ResourceState.class);
        ResourceState dog = encoded.getProperty("dog", true, ResourceState.class);
        assertThat(dog.getProperty("breed")).isEqualTo("German Shepherd");
        assertThat(dog.getProperty("name")).isNull();

        assertThat(encoded.members()).hasSize(0);
    }

    @Test
    public void testObjectWithMembersExpand() throws Exception {
        ResourceState animalProperties = new DefaultResourceState();
        animalProperties.putProperty("description", "A collection of animals");
        InMemoryCollectionResource animalsResource = new InMemoryCollectionResource(null, "animals", animalProperties);

        ResourceState dogProperties = new DefaultResourceState();
        dogProperties.putProperty("description", "A collection of dogs");
        dogProperties.putProperty("species", "Canis lupus");
        InMemoryCollectionResource dogsResource = new InMemoryCollectionResource(animalsResource, "dogs", dogProperties);

        ResourceState catProperties = new DefaultResourceState();
        catProperties.putProperty("description", "A collection of cats");
        catProperties.putProperty("species", "Felis catus");
        InMemoryCollectionResource catsResource = new InMemoryCollectionResource(animalsResource, "cats", catProperties);

        animalsResource.addMember(dogsResource);
        animalsResource.addMember(catsResource);

        ResourceState mosesState = new DefaultResourceState();
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");
        InMemoryObjectResource mosesResource = new InMemoryObjectResource(dogsResource, "Moses", mosesState);
        dogsResource.addMember(mosesResource);

        ResourceState lucyState = new DefaultResourceState("lucy");
        lucyState.putProperty("name", "Lucy Fur");
        lucyState.putProperty("breed", "Siamese-Himalayan mix");
        InMemoryObjectResource lucyResource = new InMemoryObjectResource(catsResource, "Lucy", lucyState);
        catsResource.addMember(lucyResource);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(animalsResource, "description,members(species,members(breed))", future);
        driver.encode();
        ResourceState animalsState = future.get();

        assertThat(animalsState.id()).isEqualTo("animals");
        assertThat(animalsState.getPropertyNames().size()).isEqualTo(1);
        assertThat(animalsState.getProperty("description")).isEqualTo("A collection of animals");
        assertThat(animalsState.members().size()).isEqualTo(2);

        ResourceState dogsState = animalsState.member("dogs");
        assertThat(dogsState.id()).isEqualTo("dogs");
        assertThat(dogsState.getPropertyNames().size()).isEqualTo(1);
        assertThat(dogsState.getProperty("species")).isEqualTo("Canis lupus");
        assertThat(dogsState.members().size()).isEqualTo(1);

        mosesState = dogsState.member("Moses");
        assertThat(mosesState.id()).isEqualTo("Moses");
        assertThat(mosesState.getPropertyNames().size()).isEqualTo(1);
        assertThat(mosesState.getProperty("breed")).isEqualTo("German Shepherd");
        assertThat(mosesState.members()).isEmpty();


        ResourceState catState = animalsState.member("cats");
        assertThat(catState.id()).isEqualTo("cats");
        assertThat(catState.getPropertyNames().size()).isEqualTo(1);
        assertThat(catState.getProperty("species")).isEqualTo("Felis catus");
        assertThat(catState.members().size()).isEqualTo(1);

        lucyState = catState.member("Lucy");
        assertThat(lucyState.id()).isEqualTo("Lucy");
        assertThat(lucyState.getPropertyNames().size()).isEqualTo(1);
        assertThat(lucyState.getProperty("breed")).isEqualTo("Siamese-Himalayan mix");
        assertThat(lucyState.members()).isEmpty();
    }

    @Test
    public void testObjectWithMembersExclude() throws Exception {
        ResourceState animalProperties = new DefaultResourceState();
        animalProperties.putProperty("description", "A collection of animals");
        InMemoryCollectionResource animalsResource = new InMemoryCollectionResource(null, "animals", animalProperties);

        ResourceState dogProperties = new DefaultResourceState();
        dogProperties.putProperty("description", "A collection of dogs");
        dogProperties.putProperty("species", "Canis lupus");
        InMemoryCollectionResource dogsResource = new InMemoryCollectionResource(animalsResource, "dogs", dogProperties);

        ResourceState catProperties = new DefaultResourceState();
        catProperties.putProperty("description", "A collection of cats");
        catProperties.putProperty("species", "Felis catus");
        InMemoryCollectionResource catsResource = new InMemoryCollectionResource(animalsResource, "cats", catProperties);

        animalsResource.addMember(dogsResource);
        animalsResource.addMember(catsResource);

        ResourceState mosesState = new DefaultResourceState();
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");
        InMemoryObjectResource mosesResource = new InMemoryObjectResource(dogsResource, "Moses", mosesState);
        dogsResource.addMember(mosesResource);

        ResourceState lucyState = new DefaultResourceState("lucy");
        lucyState.putProperty("name", "Lucy Fur");
        lucyState.putProperty("breed", "Siamese-Himalayan mix");
        InMemoryObjectResource lucyResource = new InMemoryObjectResource(catsResource, "Lucy", lucyState);
        catsResource.addMember(lucyResource);

        CompletableFuture<ResourceState> future = new CompletableFuture<>();
        EncodingDriver driver = createDriver(animalsResource, "description,members(*,-description,members(*,-name))", future);
        driver.encode();
        ResourceState animalsState = future.get();

        assertThat(animalsState.id()).isEqualTo("animals");
        assertThat(animalsState.getPropertyNames().size()).isEqualTo(1);
        assertThat(animalsState.getProperty("description")).isEqualTo("A collection of animals");
        assertThat(animalsState.members().size()).isEqualTo(2);

        ResourceState dogsState = animalsState.member("dogs");
        assertThat(dogsState.id()).isEqualTo("dogs");
        assertThat(dogsState.getPropertyNames().size()).isEqualTo(1);
        assertThat(dogsState.getProperty("species")).isEqualTo("Canis lupus");
        assertThat(dogsState.members().size()).isEqualTo(1);

        mosesState = dogsState.member("Moses");
        assertThat(mosesState.id()).isEqualTo("Moses");
        assertThat(mosesState.getPropertyNames().size()).isEqualTo(1);
        assertThat(mosesState.getProperty("breed")).isEqualTo("German Shepherd");
        assertThat(mosesState.members()).isEmpty();


        ResourceState catState = animalsState.member("cats");
        assertThat(catState.id()).isEqualTo("cats");
        assertThat(catState.getPropertyNames().size()).isEqualTo(1);
        assertThat(catState.getProperty("species")).isEqualTo("Felis catus");
        assertThat(catState.members().size()).isEqualTo(1);

        lucyState = catState.member("Lucy");
        assertThat(lucyState.id()).isEqualTo("Lucy");
        assertThat(lucyState.getPropertyNames().size()).isEqualTo(1);
        assertThat(lucyState.getProperty("breed")).isEqualTo("Siamese-Himalayan mix");
        assertThat(lucyState.members()).isEmpty();
    }

    /*
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

        List<ResourceState> encodedDogs = (List<ResourceState>) encoded.getProperty( "dogs" );

        assertThat( encodedDogs ).hasSize( 2 );

        assertThat( encodedDogs.get(0).getProperty("name")).isEqualTo( "Moses" );
        assertThat( encodedDogs.get(1).getProperty("name")).isEqualTo( "Only" );
    }
    */
}
