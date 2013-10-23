package org.projectodd.restafari.container.codec.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import io.netty.buffer.ByteBufOutputStream;
import org.projectodd.restafari.container.codec.*;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;
import org.projectodd.restafari.spi.resource.async.PropertyResource;
import org.projectodd.restafari.spi.resource.Resource;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class JSONEncoder implements ResourceEncoder<JSONEncoder.DriverContext> {

    public static class DriverContext {
        public JsonGenerator generator;
    }

    @Override
    public DriverContext createEncodingContext() {
        return new DriverContext();
    }

    public JSONEncoder() {
    }

    @Override
    public void encode(Resource resource, EncodingDriver<DriverContext> driver) throws IOException {
        if (driver.encodingContext().generator == null) {
            JsonFactory factory = new JsonFactory();
            ByteBufOutputStream out = new ByteBufOutputStream(driver.buffer());
            driver.encodingContext().generator = factory.createGenerator(out);
            driver.encodingContext().generator.setPrettyPrinter(new DefaultPrettyPrinter("\\n"));
        }

        if (resource instanceof ObjectResource) {
            encodeObject((ObjectResource) resource, driver);
        } else if (resource instanceof PropertyResource) {
            encodeProperty((PropertyResource) resource, driver);
        } else if (resource instanceof CollectionResource) {
            encodeCollection((CollectionResource) resource, driver);
        } else {
            System.err.println("UNKNOWN RESOURCE TYPE");
        }
    }

    protected void encodeObject(ObjectResource obj, EncodingDriver<DriverContext> driver) throws IOException {

        JsonGenerator generator = driver.encodingContext().generator;
        generator.writeStartObject();
        String id = obj.id();
        if (id != null) {
            generator.writeFieldName("id");
            generator.writeString(id);
            generator.writeFieldName("_self");
            generator.writeStartObject();
            generator.writeFieldName("href");
            generator.writeString(obj.uri().toString());
            generator.writeFieldName("type");
            generator.writeString("object");
            generator.writeEndObject();
        }
        generator.flush();
        if (driver.shouldExpand(obj)) {
            obj.writeMembers(driver);
        }
        generator.writeEndObject();
        generator.flush();
    }

    protected void encodeProperty(PropertyResource prop, EncodingDriver<DriverContext> driver) throws IOException {

        JsonGenerator generator = driver.encodingContext().generator;
        Object value = prop.get();

        generator.writeFieldName(prop.id());
        if (value instanceof Resource) {
            encode((Resource) value, driver);
        } else if (value instanceof String) {
            generator.writeString((String) value);
        } else if (value instanceof Integer) {
            generator.writeNumber((Integer) value);
        } else if (value instanceof Double) {
            generator.writeNumber((Double) value);
        } else {
            generator.writeNull();
            generator.flush();
            throw new IOException("Unkown Property Type : " + value.getClass());
        }
        generator.flush();
    }

    protected void encodeCollection(CollectionResource collection, EncodingDriver<DriverContext> driver) throws IOException {
        JsonGenerator generator = driver.encodingContext().generator;
        generator.writeStartObject();
        String id = collection.id();
        if (id != null) {
            generator.writeFieldName("id");
            generator.writeString(id);
            generator.writeFieldName("_self");
            generator.writeStartObject();
            generator.writeFieldName("href");
            generator.writeString(collection.uri().toString());
            generator.writeFieldName("type");
            generator.writeString("collection");
            generator.writeEndObject();
        }
        generator.flush();

        if (driver.shouldExpand(collection)) {
            generator.writeFieldName("members");
            generator.writeStartArray();
            generator.flush();
            collection.writeMembers(driver);
            generator.writeEndArray();
        }
        generator.writeEndObject();
        generator.flush();

    }


}
