/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec.json;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.codec.driver.StateEncodingDriver;
import io.liveoak.common.codec.json.JSONEncoder;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

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

    //TODO: use a builder for these encode methods
    protected ByteBuf encode(ResourceState resourceState, String fields) throws Exception {

        JSONEncoder encoder = new JSONEncoder();
        ByteBuf buffer = Unpooled.buffer();
        encoder.initialize(buffer);
        StateEncodingDriver driver = new StateEncodingDriver(new RequestContext.Builder().returnFields(new DefaultReturnFields(fields)).build(), encoder, resourceState);
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

        assertThat(root.get(LiveOak.ID)).isEqualTo("bob");
    }

    @Test
    public void testEmptyObjectWithURI() throws Exception {
        DefaultResourceState state = new DefaultResourceState("bob");
        state.uri( new URI("/bob") );

        ByteBuf buffer = encode( state );
        String encoded = buffer.toString(Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> root = mapper.readValue(encoded, Map.class);

        assertThat(root.get(LiveOak.ID)).isEqualTo("bob");
        assertThat(root.get(LiveOak.SELF)).isNotNull();
        assertThat(((Map) root.get(LiveOak.SELF)).get(LiveOak.HREF)).isEqualTo("/bob");
    }

    @Test
    public void testObjectWithProperties() throws Exception {
        DefaultResourceState state = new DefaultResourceState("bob");
        state.putProperty("name", "Bob McWhirter");

        ByteBuf buffer = encode(state);
        String encoded = buffer.toString(Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> root = mapper.readValue(encoded, Map.class);

        assertThat(root.get(LiveOak.ID)).isEqualTo("bob");
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

        assertThat(root.get(LiveOak.ID)).isEqualTo("bob");
        assertThat(root.get(LiveOak.SELF)).isNotNull();
        assertThat(((Map) root.get(LiveOak.SELF)).get(LiveOak.HREF)).isEqualTo("/bob");
        assertThat(root.get("name")).isEqualTo("Bob McWhirter");
    }


    @Test
    public void testObjectWithResourceStateProperty() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState("moses");
        mosesState.uri(new URI("/moses"));
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

        assertThat(root.get(LiveOak.ID).asText()).isEqualTo("bob");
        assertThat(root.get("name").asText()).isEqualTo("Bob McWhirter");

        assertThat(root.get("dog")).isNotNull();
    }

    @Test
    public void testObjectWithResourceStatePropertyWithURI() throws Exception {
        //TODO: figure out how resourceStates of resourceStates should behave with regards to link encoding or resource encoding
        DefaultResourceState mosesState = new DefaultResourceState("moses");
        //mosesState.uri( new URI("/moses") );
        mosesState.putProperty( LiveOak.HREF, "/moses" );
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

        assertThat(root.get(LiveOak.ID).asText()).isEqualTo("bob");
        assertThat(root.get(LiveOak.SELF)).isNotNull();
        assertThat(root.get(LiveOak.SELF).get(LiveOak.HREF).asText()).isEqualTo("/bob");
        assertThat(root.get("name").asText()).isEqualTo("Bob McWhirter");

        assertThat(root.get("dog")).isNotNull();
        assertThat(root.get("dog").get(LiveOak.HREF).asText()).isEqualTo("/moses");
    }

    @Test
    public void testObjectWithResourceArrayProperty() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState("moses");
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");
        mosesState.uri( new URI("/moses") );

        DefaultResourceState onlyState = new DefaultResourceState("only");
        onlyState.putProperty("name", "Only");
        onlyState.putProperty("breed", "Lab/Huskie Mix");
        onlyState.uri(new URI("/Only"));

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


        assertThat(root.get(LiveOak.ID).asText()).isEqualTo("bob");
        //assertThat(root.get("self")).isNotNull();
        //assertThat(root.get("self").get("href").asText()).isEqualTo("/bob");
        assertThat(root.get("name").asText()).isEqualTo("Bob McWhirter");

        //assertThat(root.get("dogs")).isInstanceOf(ArrayNode.class);
        assertThat(root.get("dogs")).isNotNull();

        JsonNode encodedDogs = root.get("dogs");

        System.err.println("DOGS: " + encodedDogs);

        assertThat(encodedDogs).hasSize(2);

        assertThat(encodedDogs.get(0).get(LiveOak.ID).asText()).isEqualTo("moses");
        //assertThat(encodedDogs.get(0).get("href").asText()).isEqualTo("/moses");

        assertThat(encodedDogs.get(1).get(LiveOak.ID).asText()).isEqualTo("only");
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

        assertThat(root.get(LiveOak.ID).asText()).isEqualTo("moses");
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

    @Test
    public void testResourceWithFields() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState("moses");
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");

        mosesState.uri(new URI("/moses"));

        ByteBuf buffer = encode( mosesState, "name" );
        String encoded = buffer.toString(Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(encoded);

        assertThat(root.get(LiveOak.ID).asText()).isEqualTo("moses");
        assertThat(root.get("self")).isNotNull();
        assertThat(root.get("self").get("href").asText()).isEqualTo("/moses");
        assertThat(root.get("name").asText()).isEqualTo("Moses");
        assertThat(root.get("breed")).isNull();
    }

    @Test
    public void testResourceWithExcludeIdFields() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState("moses");
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");

        mosesState.uri(new URI("/moses"));

        ByteBuf buffer = encode( mosesState, "*,-name,-id" );
        String encoded = buffer.toString(Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(encoded);

        assertThat(root.get(LiveOak.ID)).isNull();
        assertThat(root.get("self")).isNotNull();
        assertThat(root.get("self").get("href").asText()).isEqualTo("/moses");
        assertThat(root.get("breed").asText()).isEqualTo("German Shepherd");
        assertThat(root.get("name")).isNull();
    }

    @Test
    public void testResourceWithExcludeSelfFields() throws Exception {
        DefaultResourceState mosesState = new DefaultResourceState("moses");
        mosesState.putProperty("name", "Moses");
        mosesState.putProperty("breed", "German Shepherd");

        mosesState.uri(new URI("/moses"));

        ByteBuf buffer = encode( mosesState, "*,-breed,-self" );
        String encoded = buffer.toString(Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(encoded);

        assertThat(root.get(LiveOak.ID).asText()).isEqualTo("moses");
        assertThat(root.get(LiveOak.SELF)).isNull();
        assertThat(root.get("name").asText()).isEqualTo("Moses");
        assertThat(root.get("breed")).isNull();
    }

    @Test
    public void testResourceMembersWithFields() throws Exception {
        DefaultResourceState parentState = new DefaultResourceState("parent");
        parentState.uri(new URI("/parent"));
        parentState.putProperty("A", 1);
        parentState.putProperty("B", 2);

        DefaultResourceState child1State = new DefaultResourceState("child1");
        child1State.uri(new URI("/parent/child1"));
        child1State.putProperty("foo", "bar");
        child1State.putProperty("hello", "world");

        DefaultResourceState child2State = new DefaultResourceState("child2");
        child2State.uri(new URI("/parent/child2"));
        child2State.putProperty("foo", "baz");
        child2State.putProperty("goodbye", "world");

        parentState.addMember(child1State);
        parentState.addMember(child2State);

        ByteBuf buffer = encode( parentState, "B,members(hello,goodbye)");
        String encoded = buffer.toString(Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(encoded);

        assertThat(root.get(LiveOak.ID).asText()).isEqualTo("parent");
        assertThat(root.get(LiveOak.SELF).get(LiveOak.HREF).asText()).isEqualTo("/parent");
        assertThat(root.size()).isEqualTo(4);
        assertThat(root.get("B").asInt()).isEqualTo(2);

        ObjectNode child1Node = (ObjectNode)root.get(LiveOak.MEMBERS).get(0);
        assertThat(child1Node.get(LiveOak.ID).asText()).isEqualTo("child1");
        assertThat(child1Node.get(LiveOak.SELF).get(LiveOak.HREF).asText()).isEqualTo("/parent/child1");
        assertThat(child1Node.size()).isEqualTo(3);
        assertThat(child1Node.get("hello").asText()).isEqualTo("world");

        ObjectNode child2Node = (ObjectNode)root.get(LiveOak.MEMBERS).get(1);
        assertThat(child2Node.get(LiveOak.ID).asText()).isEqualTo("child2");
        assertThat(child2Node.get(LiveOak.SELF).get(LiveOak.HREF).asText()).isEqualTo("/parent/child2");
        assertThat(child2Node.size()).isEqualTo(3);
        assertThat(child2Node.get("goodbye").asText()).isEqualTo("world");
    }

    @Test
    public void testResourceMembersWithExcludeFields() throws Exception {
        DefaultResourceState parentState = new DefaultResourceState("parent");
        parentState.uri(new URI("/parent"));
        parentState.putProperty("A", 1);
        parentState.putProperty("B", 2);

        DefaultResourceState child1State = new DefaultResourceState("child1");
        child1State.uri(new URI("/parent/child1"));
        child1State.putProperty("foo", "bar");
        child1State.putProperty("hello", "world");

        DefaultResourceState child2State = new DefaultResourceState("child2");
        child2State.uri(new URI("/parent/child2"));
        child2State.putProperty("foo", "baz");
        child2State.putProperty("goodbye", "world");

        parentState.addMember(child1State);
        parentState.addMember(child2State);

        ByteBuf buffer = encode( parentState, "*,-A,members(*,-foo,-self)");
        String encoded = buffer.toString(Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(encoded);

        assertThat(root.get(LiveOak.ID).asText()).isEqualTo("parent");
        assertThat(root.get(LiveOak.SELF).get(LiveOak.HREF).asText()).isEqualTo("/parent");
        assertThat(root.size()).isEqualTo(4);
        assertThat(root.get("B").asInt()).isEqualTo(2);

        ObjectNode child1Node = (ObjectNode)root.get(LiveOak.MEMBERS).get(0);
        assertThat(child1Node.get(LiveOak.ID).asText()).isEqualTo("child1");
        assertThat(child1Node.get(LiveOak.SELF)).isNull();
        assertThat(child1Node.size()).isEqualTo(2);
        assertThat(child1Node.get("hello").asText()).isEqualTo("world");

        ObjectNode child2Node = (ObjectNode)root.get(LiveOak.MEMBERS).get(1);
        assertThat(child2Node.get(LiveOak.ID).asText()).isEqualTo("child2");
        assertThat(child2Node.get(LiveOak.SELF)).isNull();
        assertThat(child2Node.size()).isEqualTo(2);
        assertThat(child2Node.get("goodbye").asText()).isEqualTo("world");
    }


}
