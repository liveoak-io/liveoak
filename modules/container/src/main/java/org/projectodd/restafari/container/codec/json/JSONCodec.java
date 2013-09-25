package org.projectodd.restafari.container.codec.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.spi.ObjectResource;
import org.projectodd.restafari.spi.Resource;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Bob McWhirter
 */
public class JSONCodec implements ResourceCodec {

    @Override
    public ByteBuf encodeResource(Resource resource) throws IOException {
        if (!(resource instanceof ObjectResource)) {
            return null;
        }

        ObjectResource objResource = (ObjectResource) resource;

        JsonFactory factory = new JsonFactory();
        ByteBuf buf = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buf);
        JsonGenerator generator = factory.createGenerator(out);

        encodeObject(generator, objResource);

        out.close();

        return buf;
    }


    @Override
    public ByteBuf encodeResources(Collection<Resource> resources) throws IOException {

        JsonFactory factory = new JsonFactory();
        ByteBuf buf = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buf);
        JsonGenerator generator = factory.createGenerator(out);

        generator.writeStartArray();

        for ( Resource each : resources ) {
            if ( each instanceof ObjectResource ) {
                encodeObject( generator, (ObjectResource) each);
            }
        }
        generator.writeEndArray();

        out.close();
        return buf;
    }

    @Override
    public Resource decodeResource(ByteBuf resource) {
        return null;
    }

    @Override
    public Collection<Resource> decodeResources(ByteBuf resources) {
        return null;
    }

    private void encodeObject(JsonGenerator generator, ObjectResource obj) throws IOException {
        generator.writeStartObject();
        for (String propertyName : obj.getPropertyNames()) {
            Object value = obj.getProperty(propertyName);

            generator.writeFieldName(propertyName);
            if (value instanceof ObjectResource) {
                encodeObject(generator, (ObjectResource) value);
            } else if (value instanceof String) {
                generator.writeString((String) value);
            } else if (value instanceof Integer) {
                generator.writeNumber((Integer) value);
            } else if (value instanceof Double) {
                generator.writeNumber((Double) value);
            }
        }
        generator.writeEndObject();
    }

}
