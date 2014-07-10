/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec.html;

import io.liveoak.common.codec.StateEncoder;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.util.Date;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public class HTMLEncoder implements StateEncoder {

    @Override
    public void initialize(ByteBuf buffer) throws Exception {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        this.writer = factory.createXMLEventWriter(new ByteBufOutputStream(buffer));
        this.eventFactory = XMLEventFactory.newFactory();

        startTag("html");
        startTag("head");
        startTag("title");
        text("LiveOak");
        endTag("title");


        startTag("link");
        attribute("rel", "stylesheet");
        attribute("type", "text/css");
        attribute("href", "//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css");
        endTag("link");

        startTag("style");
        text(getCss());
        endTag("style");


        endTag("head");
        startTag("body");
    }

    @Override
    public void close() throws Exception {
        startTag("div");
        attribute("class", "footer");
        text("LiveOak BaaS");
        endTag("div");
        endTag("body");
        endTag("html");
        this.writer.flush();
        this.writer.close();
    }

    protected void text(String text) throws XMLStreamException {
        this.writer.add(this.eventFactory.createCharacters(text));
    }

    protected void startTag(String name) throws XMLStreamException {
        this.writer.add(this.eventFactory.createStartElement("", "", name));
    }

    protected void endTag(String name) throws XMLStreamException {
        this.writer.add(this.eventFactory.createEndElement("", "", name));
    }

    protected void attribute(String name, String value) throws XMLStreamException {
        this.writer.add(this.eventFactory.createAttribute(name, value));
    }

    @Override
    public void startResource(ResourceState resourcestate) throws Exception {
        startTag("div");
        attribute("class", "resource");


        resourceLink(resourcestate);
    }

    protected void resourceLink(ResourceState resourcestate) throws XMLStreamException {
          if ( resourcestate.uri() != null) {
              startTag("a");
              attribute("href", resourcestate.uri().toString());
              text(resourcestate.id());
              endTag("a");
              text(" / ");
          }
    }


    @Override
    public void endResource(ResourceState resourcestate) throws Exception {
        endTag("div");
    }

    @Override
    public void startProperties() throws Exception {
        startTag("table");
        startTag("tr");
        startTag("th");
        text("name");
        endTag("th");
        startTag("th");
        text("value");
        endTag("th");
        endTag("tr");
    }

    @Override
    public void endProperties() throws Exception {
        endTag("table");
    }

    @Override
    public void startProperty(String propertyName) throws Exception {
        startTag("tr");
        startTag("td");
        text(propertyName);
        endTag("td");
        startTag("td");
    }

    @Override
    public void endProperty(String propertyName) throws Exception {
        endTag("td");
        endTag("tr");
    }

    @Override
    public void startMembers() throws Exception {
        startTag("div");
        attribute("class", "members");
    }

    @Override
    public void endMembers() throws Exception {
        endTag("div");
    }

    @Override
    public void startList() throws Exception {
    }

    @Override
    public void endList() throws Exception {
    }

    @Override
    public void writeValue(String value) throws Exception {
        text(value);
    }

    @Override
    public void writeValue(Integer value) throws Exception {
        text(value.toString());
    }

    @Override
    public void writeValue(Double value) throws Exception {
        text(value.toString());
    }

    @Override
    public void writeValue(Long value) throws Exception {
        text(value.toString());
    }

    @Override
    public void writeValue(Boolean value) throws Exception {
        text(value.toString());
    }

    @Override
    public void writeValue(Date value) throws Exception {
        text(value.toString());
    }

    protected void writeValue(Object value) throws Exception {
        if ( value instanceof Map ) {
            writeValue( (Map) value );
        } else {
            writeValue( value.toString() );
        }
    }

    @Override
    public void writeValue(Map value) throws Exception {
        startTag("dl");

        for ( Object key : value.keySet() ) {
            startTag( "dt" );
            text( key.toString() );
            endTag( "dt" );

            writeValue( value.get( key ) );
        }

        endTag("dl");
    }


    @Override
    public void writeNullValue() throws Exception {
        text(""); //Set the value to empty since a null value cannot be represented in html
    }

    @Override
    public void writeLink(ResourceState resourcestate) throws Exception {
        if (resourcestate.id() != null) {
            startTag("div");
            attribute("class", "resource");
            startTag("a");
            attribute( "href", resourcestate.uri().toString() );
            text( resourcestate.id() );
            endTag("a");
            endTag("div");
        }
    }

    private String getCss() {
        return ".resource {\n" +
                "    padding: 1em;\n" +
                "}\n" +
                "\n" +
                ".resource .self {\n" +
                "    border: 1px solid black;\n" +
                "    padding: 1ex;\n" +
                "    margin-bottom: 1em;\n" +
                "}\n" +
                "\n" +
                ".resource .content {\n" +
                "    margin-left: 1em;\n" +
                "}\n" +
                "\n" +
                ".resource .resource {\n" +
                "    border-bottom: 1px solid #ccc;\n" +
                "    padding: 0;\n" +
                "    margin-bottom: .5ex;\n" +
                "}\n" +
                "\n" +
                ".resource .resource .self {\n" +
                "    border: none;\n" +
                "    padding: 0;\n" +
                "    margin: 0;\n" +
                "}\n" +
                "\n" +
                ".resource table {\n" +
                "    border: 1px solid #ccc;\n" +
                "    font-size: 90%;\n" +
                "    margin-bottom: 1em;\n" +
                "}\n" +
                "\n" +
                ".resource table tr {\n" +
                "    border-bottom: 1px solid #666;\n" +
                "}\n" +
                "\n" +
                ".resource table tr th {\n" +
                "    padding: .5ex;\n" +
                "    background-color: #ddd;\n" +
                "    border: 1px solid #666;\n" +
                "}\n" +
                "\n" +
                ".resource table tr td {\n" +
                "    padding: .5ex;\n" +
                "    border: 1px solid #666;\n" +
                "}\n" +
                "\n" +
                ".resource .members {\n" +
                "    padding: 1ex;\n" +
                "    border: 1px solid #ccc;\n" +
                "}\n" +
                "\n" +
                ".footer {\n" +
                "    border-top: 1px solid #ddd;\n" +
                "    padding-left: 1em;\n" +
                "    color: #999;\n" +
                "    font-size: 80%;\n" +
                "}\n";
    }

    private XMLEventWriter writer;
    private XMLEventFactory eventFactory;
}
