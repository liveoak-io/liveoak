package io.liveoak.container.codec.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.container.codec.ResourceDecoder;
import io.liveoak.spi.state.ResourceState;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Bob McWhirter
 */
public class JSONDecoder implements ResourceDecoder {

    public JSONDecoder() {
    }

    @Override
    public ResourceState decode(ByteBuf resource) throws IOException {
        JsonFactory factory = new JsonFactory();
        factory.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        factory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        ByteBufInputStream in = new ByteBufInputStream(resource);
        JsonParser parser = factory.createParser(in);
        parser.nextToken();

        ResourceState result = decode(parser);

        if (result == null) {
            result = new DefaultResourceState();
        }

        return result;
    }


    protected ResourceState decode(JsonParser parser) throws IOException {
        Object value = decodeValue(parser);
        if (value instanceof ResourceState) {
            return (ResourceState) value;
        }

        return null;
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

        return null;
    }

    protected ResourceState decodeObject(JsonParser parser) throws IOException {
        parser.nextToken();
        DefaultResourceState resource = new DefaultResourceState();

        while (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
            decodeProperty(parser, resource);
        }

        parser.nextToken();

        return resource;
    }

    protected ArrayList<Object> decodeArray(JsonParser parser) throws IOException {

        parser.nextToken();
        ArrayList<Object> array = new ArrayList<>();

        while (parser.getCurrentToken() != JsonToken.END_ARRAY) {
            Object value = decodeValue(parser);
            array.add(value);
        }

        parser.nextToken();

        return array;
    }

    protected void decodeProperty(JsonParser parser, ResourceState state) throws IOException {
        String name = parser.getText();
        parser.nextToken();
        Object value = decodeValue(parser);

        if (name.equals("_members") && value instanceof Collection) {
            ((Collection) value).stream().forEach((e) -> {
                state.addMember((ResourceState) e);
            });
        } else {
            state.putProperty(name, value);
        }

        if (name.equals("id")) {
            state.id(value.toString());
        }
    }
}
