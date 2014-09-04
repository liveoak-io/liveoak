/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec.json;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import io.liveoak.common.codec.NonEncodableValueException;
import io.liveoak.common.codec.StateEncoder;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

/**
 * @author Bob McWhirter
 */
public class JSONEncoder implements StateEncoder {

    public JSONEncoder() {
    }

    public JSONEncoder(boolean inhibitIds) {
        this.inhibitIds = inhibitIds;
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
    public void startResource(ResourceState resourceState) throws Exception {
        this.generator.writeStartObject();
        if (this.inhibitIds) {
            return;
        }
        if (resourceState.id() != null) {
            this.generator.writeFieldName("id");
            this.generator.writeString(resourceState.id());
        }
        if (resourceState.uri() != null) {
            this.generator.writeFieldName("self");
            this.generator.writeStartObject();
            this.generator.writeFieldName("href");
            this.generator.writeString(resourceState.uri().toString());
            this.generator.writeEndObject();
        }
    }

    @Override
    public void endResource(ResourceState resourceState) throws IOException {
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
        this.generator.writeFieldName("members");
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
    public void writeValue(Long value) throws Exception {
        this.generator.writeNumber(value);
    }

    @Override
    public void writeValue(Boolean value) throws Exception {
        this.generator.writeBoolean(value);
    }

    @Override
    public void writeValue(Date value) throws Exception {
        this.generator.writeNumber(value.getTime());
    }

    @Override
    public void writeNullValue() throws Exception {
        this.generator.writeNull();
    }

    protected void writeValue(Object value) throws Exception {
        if (value instanceof String) {
            writeValue((String) value);
        } else if (value instanceof Integer) {
            writeValue((Integer) value);
        } else if (value instanceof Double) {
            writeValue((Double) value);
        } else if (value instanceof Long) {
            writeValue((Long) value);
        } else if (value instanceof Boolean) {
            writeValue((Boolean) value);
        } else {
            throw new NonEncodableValueException(value);
        }
    }

    @Override
    public void writeValue(Map value) throws Exception {
        this.generator.writeStartObject();
        for (Object key : value.keySet()) {
            this.generator.writeFieldName(key.toString());
            writeValue(value.get(key));
        }
        this.generator.writeEndObject();
    }

    @Override
    public void writeLink(ResourceState resourceState) throws Exception {
        this.generator.writeStartObject();
        this.generator.writeFieldName("id");
        this.generator.writeString(resourceState.id());
        this.generator.writeFieldName("href");
        this.generator.writeString(resourceState.uri().toString());
        this.generator.writeEndObject();

    }

    public static final String jsonStringEscape(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '/':
                    sb.append("\\/");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\'':
                    sb.append("\\\'");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private boolean inhibitIds = false;
    private JsonGenerator generator;
}
