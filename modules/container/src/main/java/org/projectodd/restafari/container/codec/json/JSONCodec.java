package org.projectodd.restafari.container.codec.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.projectodd.restafari.container.SimpleObjectResource;
import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.spi.ObjectResource;
import org.projectodd.restafari.spi.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class JSONCodec implements ResourceCodec {

    @Override
    public ByteBuf encode(Resource resource) throws IOException {
        if (!(resource instanceof ObjectResource)) {
            return null;
        }

        ObjectResource objResource = (ObjectResource) resource;

        ByteBuf buf = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buf);
        JsonFactory factory = new JsonFactory();
        JsonGenerator generator = factory.createGenerator(out);

        encodeObject(generator, objResource);

        generator.close();
        out.close();

        return buf;
    }


    @Override
    public ByteBuf encode(Collection<Resource> resources) throws IOException {

        JsonFactory factory = new JsonFactory();
        ByteBuf buf = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buf);
        JsonGenerator generator = factory.createGenerator(out);

        generator.writeStartArray();

        for (Resource each : resources) {
            if (each instanceof ObjectResource) {
                encodeObject(generator, (ObjectResource) each);
            }
        }
        generator.writeEndArray();

        out.close();
        return buf;
    }

    protected void encodeObject(JsonGenerator generator, ObjectResource obj) throws IOException {
        System.err.println( "startObject" );
        generator.writeStartObject();
        for (String propertyName : obj.getPropertyNames()) {
            Object value = obj.getProperty(propertyName);

            System.err.println( "write firled: " + propertyName );
            generator.writeFieldName(propertyName);
            if (value instanceof ObjectResource) {
                encodeObject(generator, (ObjectResource) value);
            } else if (value instanceof String) {
                System.err.println( "write value: " + value );
                generator.writeString((String) value);
            } else if (value instanceof Integer) {
                generator.writeNumber((Integer) value);
            } else if (value instanceof Double) {
                generator.writeNumber((Double) value);
            }
        }
        generator.writeEndObject();
    }

    @Override
    public Object decode(ByteBuf resource) throws IOException {
        JsonFactory factory = new JsonFactory();
        ByteBufInputStream in = new ByteBufInputStream(resource);
        JsonParser parser = factory.createParser(in);

        return decode(parser);
    }

    protected Object decode(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();

        if (token == JsonToken.VALUE_STRING) {
            return parser.getValueAsString();
        }

        if (token == JsonToken.VALUE_NUMBER_INT) {
            return parser.getValueAsInt();
        }

        if (token == JsonToken.VALUE_NUMBER_FLOAT) {
            return parser.getValueAsDouble();
        }

        if (token == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        }

        if (token == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE;
        }

        if (token == JsonToken.START_OBJECT) {
            return decodeObject(parser);
        }

        if (token == JsonToken.START_ARRAY) {
            return decodeArray(parser);
        }

        return null;
    }

    protected ObjectResource decodeObject(JsonParser parser) throws IOException {
        SimpleObjectResource resource = new SimpleObjectResource();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String name = parser.getCurrentName();
            Object value = decode(parser);
            resource.setProperty(name, value);
        }

        return resource;
    }

    protected Collection<Object> decodeArray(JsonParser parser) throws IOException {

        List<Object> array = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            array.add(decode(parser));
        }

        return array;
    }


}
