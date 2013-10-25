package org.projectodd.restafari.container.codec.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import org.projectodd.restafari.container.codec.ResourceEncoder;
import org.projectodd.restafari.container.codec.EncodingContext;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.BinaryResource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;
import org.projectodd.restafari.spi.resource.async.PropertyResource;

import java.nio.charset.Charset;

/**
 * @author Bob McWhirter
 */
public class JSONEncoder implements ResourceEncoder<JsonGenerator> {

    private static ByteBuf CURRENT = null;

    public JsonGenerator createAttachment(ByteBuf buffer) throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        JsonGenerator generator = factory.createGenerator(out);
        generator.setPrettyPrinter(new DefaultPrettyPrinter("\\n"));
        CURRENT = buffer;
        return generator;
    }

    public void encode(EncodingContext<JsonGenerator> context) throws Exception {
        Object o = context.object();
        JsonGenerator generator = context.attachment();
        if (o instanceof CollectionResource) {
            encodeCollection(context);
        } else if (o instanceof ObjectResource) {
            encodeObject(context);
        } else if (o instanceof PropertyResource) {
            encodeProperty(context);
        } else if (o instanceof BinaryResource) {
            encodeBinary(context);
        } else {
            encodeValue(context);
        }
        generator.flush();
    }

    @Override
    public void close(EncodingContext<JsonGenerator> context) throws Exception {
        JsonGenerator generator = context.attachment();
        generator.flush();
        generator.close();
    }


    protected void encodeCollection(EncodingContext<JsonGenerator> context) throws Exception {
        JsonGenerator generator = context.attachment();
        generator.writeStartObject();
        encodeProlog(context);

        if (context.shouldEncodeContent()) {
            generator.writeFieldName("content");
            generator.writeStartArray();
            context.encodeContent(() -> {
                try {
                    generator.writeEndArray();
                    generator.writeEndObject();
                    context.end();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            generator.writeEndObject();
            context.end();
        }
    }

    protected void encodeObject(EncodingContext<JsonGenerator> context) throws Exception {
        JsonGenerator generator = context.attachment();
        generator.writeStartObject();
        encodeProlog(context);
        context.encodeContent(() -> {
            try {
                generator.writeEndObject();
                context.end();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    protected void encodeProperty(EncodingContext<JsonGenerator> context) throws Exception {
        Resource prop = (Resource) context.object();
        JsonGenerator generator = context.attachment();

        if (context.depth() > 0) {
            generator.writeFieldName(prop.id());
        }
        context.encodeContent(() -> {
            context.end();
        });
    }

    protected void encodeBinary(EncodingContext<JsonGenerator> context) throws Exception {

        JsonGenerator generator = context.attachment();
        generator.writeStartObject();
        encodeProlog(context);

        generator.writeFieldName("content");
        context.encodeContent(() -> {
            try {
                generator.writeEndObject();
                context.end();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    protected void encodeValue(EncodingContext<JsonGenerator> context) throws Exception {

        Object value = context.object();
        JsonGenerator generator = context.attachment();

        if (value instanceof String) {
            generator.writeString(value.toString());
        } else if (value instanceof Double) {
            generator.writeNumber((Double) value);
        } else if (value instanceof Float) {
            generator.writeNumber((Float) value);
        } else if (value instanceof Short) {
            generator.writeNumber((Short) value);
        } else if (value instanceof Integer) {
            generator.writeNumber((Integer) value);
        } else if (value instanceof Long) {
            generator.writeNumber((Long) value);
        } else if (value instanceof ByteBuf) {
            byte[] bytes = new byte[((ByteBuf) value).readableBytes()];
            ((ByteBuf) value).readBytes(bytes);
            generator.writeBinary(bytes);
        } else {
            System.err.println("UNKNOWN VALUE TYPE: " + value + " // " + ((value == null) ? null : value.getClass()));
        }

        context.end();
    }

    protected void encodeProlog(EncodingContext<JsonGenerator> context) throws Exception {
        JsonGenerator generator = context.attachment();
        Resource resource = (Resource) context.object();
        String id = resource.id();
        if (id != null) {
            generator.writeFieldName("id");
            generator.writeString(id);
            generator.writeFieldName("_self");
            generator.writeStartObject();
            generator.writeFieldName("href");
            generator.writeString(resource.uri().toString());
            generator.writeFieldName("type");
            if (resource instanceof CollectionResource) {
                generator.writeString("collection");
            } else if (resource instanceof ObjectResource) {
                generator.writeString("object");
            } else if (resource instanceof BinaryResource) {
                generator.writeString("binary");
            }
            generator.writeEndObject();
        }
    }

}
