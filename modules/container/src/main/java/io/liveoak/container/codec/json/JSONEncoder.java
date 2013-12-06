/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import io.liveoak.container.codec.Encoder;
import io.liveoak.container.codec.NonEncodableValueException;
import io.liveoak.spi.resource.async.Resource;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

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
        if (resource.id() != null) {
            this.generator.writeFieldName("id");
            this.generator.writeString(resource.id());
            this.generator.writeFieldName("self");
            this.generator.writeStartObject();
            this.generator.writeFieldName("href");
            this.generator.writeString(resource.uri().toString());
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
        this.generator.writeFieldName(propertyName);
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
        this.generator.writeFieldName("_members");
        this.generator.writeStartArray();
    }

    @Override
    public void endMembers() throws Exception {
        this.generator.writeEndArray();
    }

    // ----------------------------------------

    @Override
    public void writeValue(String value) throws Exception {
        this.generator.writeString(value);
    }

    @Override
    public void writeValue(Integer value) throws Exception {
        this.generator.writeNumber(value);
    }

    @Override
    public void writeValue(Double value) throws Exception {
        this.generator.writeNumber(value);
    }

    @Override
    public void writeValue( Long value ) throws Exception {
        this.generator.writeNumber(value);
    }

    @Override
    public void writeValue( Boolean value ) throws Exception {
        this.generator.writeBoolean( value );
    }

    @Override
    public void writeValue(Date value) throws Exception {
        this.generator.writeNumber(value.getTime());
    }

    protected void writeValue(Object value) throws Exception {
        if ( value instanceof String ) {
            writeValue( (String) value );
        } else if ( value instanceof Integer ) {
            writeValue( (Integer) value );
        } else if ( value instanceof Double ) {
            writeValue( (Double) value);
        } else if ( value instanceof Long ) {
            writeValue( (Long) value);
        } else if ( value instanceof Boolean ) {
            writeValue( (Boolean) value );
        } else {
            throw new NonEncodableValueException( value );
        }
    }

    @Override
    public void writeValue(Map value) throws Exception {
        this.generator.writeStartObject();
        for ( Object key : value.keySet() ) {
            this.generator.writeFieldName( key.toString() );
            writeValue( value.get( key ) );
        }
        this.generator.writeEndObject();
    }

    public void writeLink(Resource resource) throws Exception {
        this.generator.writeStartObject();
        this.generator.writeFieldName("id");
        this.generator.writeString(resource.id());
        this.generator.writeFieldName("href");
        this.generator.writeString(resource.uri().toString());
        this.generator.writeEndObject();

    }

    private JsonGenerator generator;
}
