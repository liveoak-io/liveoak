package io.liveoak.container.codec.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.liveoak.container.codec.Encoder;
import io.liveoak.spi.resource.async.Resource;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

/**
 * @author Bob McWhirter
 */
public class JSONEncoder implements Encoder {

    public JSONEncoder() {
    }

    @Override
    public void initialize(ByteBuf buffer) throws Exception {
        JsonFactory factory = new JsonFactory();
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        this.generator = factory.createGenerator(out);
        this.generator.setPrettyPrinter(new DefaultPrettyPrinter("\\n"));
    }

    @Override
    public void close() throws Exception {
        this.generator.flush();
        this.generator.close();
    }

    // ----------------------------------------

    @Override
    public void startResource(Resource resource) throws Exception {
        this.generator.writeStartObject();
        if ( resource.id() != null ) {
            this.generator.writeFieldName( "id" );
            this.generator.writeString(resource.id());
            this.generator.writeFieldName("self");
            this.generator.writeStartObject();
            this.generator.writeFieldName("href");
            this.generator.writeString( resource.uri().toString() );
            this.generator.writeEndObject();
        }
    }

    @Override
    public void endResource(Resource resource) throws IOException {
        this.generator.writeEndObject();
    }

    @Override
    public void startProperties() throws Exception {
        // not needed
    }

    @Override
    public void endProperties() throws Exception {
        // not needed
    }

    // ----------------------------------------

    @Override
    public void startProperty(String propertyName) throws Exception {
        this.generator.writeFieldName( propertyName );
    }

    @Override
    public void endProperty(String propertyName) throws Exception {
        // not used in JSON
    }

    // ----------------------------------------

    @Override
    public void startList() throws Exception {
        this.generator.writeStartArray();
    }

    @Override
    public void endList() throws Exception {
        this.generator.writeEndArray();
    }

    // ----------------------------------------

    @Override
    public void startMembers() throws Exception {
        this.generator.writeFieldName( "_members" );
        this.generator.writeStartArray();
    }

    @Override
    public void endMembers() throws Exception {
        this.generator.writeEndArray();
    }

    // ----------------------------------------

    @Override
    public void writeValue(String value) throws Exception {
        this.generator.writeString( value );
    }

    @Override
    public void writeValue(Integer value) throws Exception {
        this.generator.writeNumber( value );
    }

    @Override
    public void writeValue(Double value) throws Exception {
        this.generator.writeNumber( value );
    }

    @Override
    public void writeValue(Date value) throws Exception {

    }

    public void writeLink(Resource resource) throws Exception {
        this.generator.writeStartObject();
        this.generator.writeFieldName( "id" );
        this.generator.writeString( resource.id() );
        this.generator.writeFieldName( "href" );
        this.generator.writeString( resource.uri().toString() );
        this.generator.writeEndObject();

    }

    private JsonGenerator generator;
}
