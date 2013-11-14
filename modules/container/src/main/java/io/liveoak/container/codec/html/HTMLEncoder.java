package io.liveoak.container.codec.html;

import io.liveoak.container.codec.Encoder;
import io.liveoak.spi.resource.async.Resource;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class HTMLEncoder implements Encoder {

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

        startTag("link");
        attribute("rel", "stylesheet");
        attribute("type", "text/css");
        attribute("href", "/css/liveoak.css");
        endTag("link");


        endTag("head");
        startTag("body");
    }

    @Override
    public void close() throws Exception {
        startTag( "div" );
        attribute( "class", "footer" );
        text( "LiveOak BaaS" );
        endTag( "div" );
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
    public void startResource(Resource resource) throws Exception {
        startTag("div");
        attribute("class", "resource");


        resourceLink(resource);
    }

    protected void resourceLink(Resource resource) throws XMLStreamException {
        List<Resource> lineage = new ArrayList<>();

        Resource current = resource;

        while (current != null) {
            lineage.add(0, current);
            current = current.parent();
        }

        if (!lineage.get(0).id().equals("")) {
            startTag("a");
            attribute("href", "/");
            text("ROOT");
            endTag("a");
            text(" / ");
        }

        for (Resource each : lineage) {
            startTag("a");
            attribute("href", each.uri().toString());

            String id = each.id();
            if ("".equals(id)) {
                id = "ROOT";
            }

            text(id);
            endTag("a");
            text(" / ");
        }
    }


    @Override
    public void endResource(Resource resource) throws Exception {
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
    public void writeValue(Date value) throws Exception {
        text(value.toString());
    }

    @Override
    public void writeLink(Resource resource) throws Exception {
        startTag( "div" );
        attribute( "class", "resource" );
        startTag( "a" );
        attribute( "href", resource.uri().toString() );
        text( resource.id() );
        endTag( "a" );
        endTag( "div" );
    }

    private XMLEventWriter writer;
    private XMLEventFactory eventFactory;
}
