package org.projectodd.restafari.container.codec.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.projectodd.restafari.container.codec.DefaultCollectionResourceState;
import org.projectodd.restafari.container.codec.DefaultObjectResourceState;
import org.projectodd.restafari.container.codec.DefaultPropertyResourceState;
import org.projectodd.restafari.container.codec.ResourceDecoder;
import org.projectodd.restafari.spi.state.CollectionResourceState;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.spi.state.PropertyResourceState;
import org.projectodd.restafari.spi.state.ResourceState;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 */
public class JSONDecoder implements ResourceDecoder {

    private static final Set<String> SIMPLE_COLLECTION_PROPERTIES = new HashSet<String>() {{
        add("id");
        add("self");
        add("members");
    }};

    public JSONDecoder() {
    }

    @Override
    public ResourceState decode(ByteBuf resource) throws IOException {

        JsonFactory factory = new JsonFactory();
        ByteBufInputStream in = new ByteBufInputStream(resource);
        JsonParser parser = factory.createParser(in);
        parser.nextToken();

        ResourceState result = decode(parser);
        if (result instanceof ObjectResourceState) {
            result = possiblyConvertToCollection((ObjectResourceState) result);
        }

        return result;
    }

    protected ResourceState possiblyConvertToCollection(ObjectResourceState state) {

        List<PropertyResourceState> p = state.members().collect(Collectors.toList());

        Object selfProp = state.getProperty("_self");
        Object membersProp = null;

        boolean isCollection = false;

        if (selfProp != null && selfProp instanceof ObjectResourceState) {
            Object typeProp = ((ObjectResourceState) selfProp).getProperty("type");
            if (typeProp != null && "collection".equals(typeProp)) {
                isCollection = true;
            }
        }

        membersProp = state.getProperty("members");
        if (membersProp != null && membersProp instanceof CollectionResourceState) {
            isCollection = true;
        } else {
            membersProp = null;
        }

        if (isCollection) {
            DefaultCollectionResourceState collection = new DefaultCollectionResourceState(state.id());
            if (membersProp != null) {
                ((CollectionResourceState) membersProp).members().forEach((m) -> {
                    collection.addResource(m);
                });
            }
            return collection;
        }

        return state;
    }


    protected ResourceState decode(JsonParser parser) throws IOException {
        Object value = decodeValue(parser);
        if (value instanceof ResourceState) {
            return (ResourceState) value;
        }

        return new DefaultPropertyResourceState(null, value);
    }

    protected Object decodeValue(JsonParser parser) throws IOException {
        JsonToken token = parser.getCurrentToken();

        Object value = null;

        if (token == JsonToken.VALUE_STRING) {
            value = parser.getValueAsString();
            parser.nextToken();
            return value;
        }

        if (token == JsonToken.VALUE_NUMBER_INT) {
            value = parser.getValueAsInt();
            parser.nextToken();
            return value;
        }

        if (token == JsonToken.VALUE_NUMBER_FLOAT) {
            value = parser.getValueAsDouble();
            parser.nextToken();
            return value;
        }

        if (token == JsonToken.VALUE_FALSE) {
            value = Boolean.FALSE;
            parser.nextToken();
            return value;
        }

        if (token == JsonToken.VALUE_TRUE) {
            value = (Boolean.TRUE);
            parser.nextToken();
            return value;
        }

        if (token == JsonToken.START_OBJECT) {
            return decodeObject(parser);
        }

        if (token == JsonToken.START_ARRAY) {
            return decodeArray(parser);
        }

        if (token == JsonToken.FIELD_NAME) {
            return decodeProperty(parser);
        }

        return null;
    }

    protected ObjectResourceState decodeObject(JsonParser parser) throws IOException {
        parser.nextToken();
        DefaultObjectResourceState resource = new DefaultObjectResourceState();

        while (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
            PropertyResourceState property = decodeProperty(parser);
            resource.addProperty(property);
        }

        parser.nextToken();

        return resource;
    }

    protected CollectionResourceState decodeArray(JsonParser parser) throws IOException {

        parser.nextToken();
        DefaultCollectionResourceState array = new DefaultCollectionResourceState();

        while (parser.getCurrentToken() != JsonToken.END_ARRAY) {
            ResourceState o = decode(parser);
            if (o != null) {
                array.addResource(o);
            }
        }

        parser.nextToken();

        return array;
    }

    protected PropertyResourceState decodeProperty(JsonParser parser) throws IOException {
        String name = parser.getText();
        parser.nextToken();
        Object value = decodeValue(parser);
        return new DefaultPropertyResourceState(name, value);
    }

}
