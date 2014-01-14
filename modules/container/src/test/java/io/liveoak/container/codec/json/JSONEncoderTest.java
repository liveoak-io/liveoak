/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.liveoak.common.codec.driver.StateEncodingDriver;
import io.liveoak.common.codec.json.JSONEncoder;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.codec.driver.EncodingDriver;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class JSONEncoderTest {

    protected ByteBuf encode(ResourceState resourceState) throws Exception {

        JSONEncoder encoder = new JSONEncoder();
        ByteBuf buffer = Unpooled.buffer();
        encoder.initialize(buffer);
        StateEncodingDriver driver = new StateEncodingDriver(new RequestContext.Builder().build(), encoder, resourceState);
        driver.encode();
        driver.close();
        return buffer;
    }

    @Test
    public void testEmptyObject() throws Exception {
        DefaultResourceState state = new DefaultResourceState("bob");

        ByteBuf buffer = encode( state );
        String encoded = buffer.toString(Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> root = mapper.readValue(encoded, Map.class);

        assertThat(root.get("id")).isEqualTo("bob");
    }

    @Test
    public void testEmptyObjectWithURI() throws Exception {
        DefaultResourceState state = new DefaultResourceState("bob");
        state.uri( new URI("/bob") );

        ByteBuf buffer = encode( state );
        String encoded = buffer.toString(Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> root = mapper.readValue(encoded, Map.class);

        assertThat(root.get("id")).isEqualTo("bob");
        assertThat(root.get("self")).isNotNull();
        assertThat(((Map) root.get("self")).get("href")).isEqualTo("/bob");
    }

    @Test
    public void testObjectWithProperties() throws Exception {
        DefaultResourceState state = new DefaultResourceState("bob");
        state.putProperty("name", "Bob McWhirter");

        ByteBuf buffer = encode(state);
        String encoded = buffer.toString(Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> root = mapper.readValue(encoded, Map.class);

        assertThat(root.get("id")).isEqualTo("bob");
        assertThat(root.get("name")).isEqualTo("Bob McWhirter");
    }

    @Test
    public void testObjectWithPropertiesWithURI() throws Exception {
        DefaultResourceState state = new DefaultResourceState("bob");
        state.uri( new URI("/bob") );
        state.putProperty("name", "Bob McWhirter");

        ByteBuf buffer = encode(state);
        String encoded = buffer.toString(Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> root = mapper.readValue(encoded, Map.class);

        assertThat(root.get("id")).isEqualTo("bob");
        assertThat(root.get("self")).isNotNull();
        assertThat(((Map) root.get("self")).get("href")).isEqualTo("/bob");
        assertThat(root.get("name")).isEqualTo("Bob McWhirter");
    }


    @Test
    public void testObjectWithResourceStateProperty() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState("moses");
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");

        DefaultResourceState bobState = new DefaultResourceState("bob");

        bobState.putProperty("name", "Bob McWhirter");
        bobState.putProperty("dog", mosesState);

        ByteBuf buffer = encode(bobState);

        String encoded = buffer.toString(Charset.defaultCharset());

        System.err.println(encoded);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(encoded);

        assertThat(root.get("id").asText()).isEqualTo("bob");
        assertThat(root.get("name").asText()).isEqualTo("Bob McWhirter");

        assertThat(root.get("dog")).isNotNull();
    }

    @Test
    public void testObjectWithResourceStatePropertyWithURI() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState("moses");
        mosesState.uri( new URI("/moses") );
        //mosesState.putProperty("name", "Moses");
        //mosesState.putProperty("breed", "German Shepherd");

        DefaultResourceState bobState = new DefaultResourceState("bob");
        bobState.uri(new URI("/bob"));
        bobState.putProperty("name", "Bob McWhirter");
        bobState.putProperty("dog", mosesState);

        ByteBuf buffer = encode(bobState);

        String encoded = buffer.toString(Charset.defaultCharset());

        System.err.println(encoded);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(encoded);

        assertThat(root.get("id").asText()).isEqualTo("bob");
        assertThat(root.get("self")).isNotNull();
        assertThat(root.get("self").get("href").asText()).isEqualTo("/bob");
        assertThat(root.get("name").asText()).isEqualTo("Bob McWhirter");

        assertThat(root.get("dog")).isNotNull();
        assertThat(root.get("dog").get("href").asText()).isEqualTo("/moses");
    }

    @Test
    public void testObjectWithResourceArrayProperty() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState("moses");
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");

        DefaultResourceState onlyState = new DefaultResourceState("only");
        onlyState.putProperty("name", "Only");
        onlyState.putProperty("breed", "Lab/Huskie Mix");

        DefaultResourceState bobState = new DefaultResourceState("bob");
        bobState.putProperty("name", "Bob McWhirter");
        ArrayList<ResourceState> dogs = new ArrayList<>();
        dogs.add(mosesState);
        dogs.add(onlyState);
        bobState.putProperty("dogs", dogs);

        ByteBuf buffer = encode(bobState);

        String encoded = buffer.toString(Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(encoded);


        assertThat(root.get("id").asText()).isEqualTo("bob");
        //assertThat(root.get("self")).isNotNull();
        //assertThat(root.get("self").get("href").asText()).isEqualTo("/bob");
        assertThat(root.get("name").asText()).isEqualTo("Bob McWhirter");

        //assertThat(root.get("dogs")).isInstanceOf(ArrayNode.class);
        assertThat(root.get("dogs")).isNotNull();

        JsonNode encodedDogs = root.get("dogs");

        System.err.println("DOGS: " + encodedDogs);

        assertThat(encodedDogs).hasSize(2);

        assertThat(encodedDogs.get(0).get("id").asText()).isEqualTo("moses");
        //assertThat(encodedDogs.get(0).get("href").asText()).isEqualTo("/moses");

        assertThat(encodedDogs.get(1).get("id").asText()).isEqualTo("only");
        //assertThat(encodedDogs.get(1).get("href").asText()).isEqualTo("/only");

    }

    @Test
    public void testResourceWithMapValue() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState("moses");
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");
        mosesState.putProperty("feet", new HashMap() {{
            put( "left", "brown" );
            put( "right", "missing" );
        }});

        ByteBuf buffer = encode( mosesState );
        String encoded = buffer.toString(Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(encoded);

        assertThat(root.get("id").asText()).isEqualTo("moses");
        //assertThat(root.get("self")).isNotNull();
        //assertThat(root.get("self").get("href").asText()).isEqualTo("/moses");
        assertThat(root.get("name").asText()).isEqualTo("Moses");
        assertThat(root.get("breed").asText()).isEqualTo("German Shepherd");

        JsonNode feet = root.get( "feet" );

        assertThat( feet ).isNotNull();
        assertThat( feet ).hasSize( 2 );
        assertThat( feet.get( "left" ).asText() ).isEqualTo( "brown" );
        assertThat( feet.get( "right" ).asText() ).isEqualTo( "missing" );
    }
}
