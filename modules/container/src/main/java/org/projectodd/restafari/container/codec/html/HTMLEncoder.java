package org.projectodd.restafari.container.codec.html;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import org.projectodd.restafari.container.codec.EncodingContext;
import org.projectodd.restafari.container.codec.ResourceEncoder;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.BinaryResource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;
import org.projectodd.restafari.spi.resource.async.PropertyResource;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

/**
 * @author Bob McWhirter
 */
public class HTMLEncoder implements ResourceEncoder<HTMLEncoder.EncoderState> {

    public static class EncoderState {
        public XMLEventWriter writer;
        public XMLEventFactory factory;
    }

    @Override
    public EncoderState createAttachment(ByteBuf output) throws Exception {
        EncoderState state = new EncoderState();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        state.writer = factory.createXMLEventWriter(new ByteBufOutputStream(output));
        state.factory = XMLEventFactory.newFactory();

        state.writer.add(state.factory.createStartDocument());
        return state;
    }

    @Override
    public void close(EncodingContext<EncoderState> context) throws Exception {
        EncoderState state = context.attachment();
        XMLEventWriter writer = context.attachment().writer;
        XMLEventFactory factory = context.attachment().factory;

        writer.add(factory.createEndDocument());

        state.writer.flush();
        state.writer.close();
    }

    @Override
    public void encode(EncodingContext<EncoderState> context) throws Exception {
        Object o = context.object();
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
    }

    protected void encodeCollection(EncodingContext<EncoderState> context) throws Exception {
        Resource resource = (Resource) context.object();
        XMLEventWriter writer = context.attachment().writer;
        XMLEventFactory factory = context.attachment().factory;

        writer.add(factory.createStartElement("", "", "div"));

        writer.add(factory.createStartElement("", "", "div"));
        if (context.depth() == 0 && resource.parent() != null) {
            writer.add(factory.createStartElement("", "", "a"));
            writer.add(factory.createAttribute("href", resource.parent().uri().toString()));
            writer.add(factory.createCharacters(resource.parent().id()));
            writer.add(factory.createEndElement("", "", "a"));
            writer.add(factory.createCharacters(" : "));
        }
        writer.add(factory.createStartElement("", "", "b"));
        writer.add(factory.createStartElement("", "", "a"));
        writer.add(factory.createAttribute("href", resource.uri().toString()));
        writer.add(factory.createCharacters(resource.id()));
        writer.add(factory.createEndElement("", "", "b"));
        writer.add(factory.createEndElement("", "", "div"));

        if (context.shouldEncodeContent()) {
            writer.add(factory.createStartElement("", "", "div"));
            writer.add(factory.createAttribute("class", "content"));
            writer.add(factory.createStartElement("", "", "div"));
            writer.add(factory.createStartElement("", "", "b"));
            writer.add(factory.createCharacters("content"));
            writer.add(factory.createEndElement("", "", "b"));
            writer.add(factory.createEndElement("", "", "div"));
            context.encodeContent(() -> {
                try {
                    writer.add(factory.createEndElement("", "", "div"));
                    writer.add(factory.createEndElement("", "", "div"));
                    context.end();
                } catch (XMLStreamException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            });
        } else {
            writer.add(factory.createEndElement("", "", "div"));
            context.end();
        }
    }

    protected void encodeObject(EncodingContext<EncoderState> context) throws Exception {
        Resource resource = (Resource) context.object();
        XMLEventWriter writer = context.attachment().writer;
        XMLEventFactory factory = context.attachment().factory;

        writer.add(factory.createStartElement("", "", "div"));

        writer.add(factory.createStartElement("", "", "div"));

        if (context.depth() == 0 && resource.parent() != null) {
            writer.add(factory.createStartElement("", "", "a"));
            writer.add(factory.createAttribute("href", resource.parent().uri().toString()));
            writer.add(factory.createCharacters(resource.parent().id()));
            writer.add(factory.createEndElement("", "", "a"));
            writer.add(factory.createCharacters(" : "));
        }

        writer.add(factory.createStartElement("", "", "b"));
        writer.add(factory.createStartElement("", "", "a"));
        writer.add(factory.createAttribute("href", resource.uri().toString()));
        writer.add(factory.createCharacters(resource.id()));
        writer.add(factory.createEndElement("", "", "b"));
        writer.add(factory.createEndElement("", "", "div"));

        // ----------------------------------------

        if (context.shouldEncodeContent()) {
            writer.add(factory.createStartElement("", "", "table"));
            writer.add(factory.createStartElement("", "", "tr"));

            writer.add(factory.createStartElement("", "", "th"));
            writer.add(factory.createCharacters("key"));
            writer.add(factory.createEndElement("", "", "th"));

            writer.add(factory.createStartElement("", "", "th"));
            writer.add(factory.createCharacters("value"));
            writer.add(factory.createEndElement("", "", "th"));

            writer.add(factory.createEndElement("", "", "tr"));

            context.encodeContent(() -> {
                try {
                    writer.add(factory.createEndElement("", "", "table"));
                    writer.add(factory.createEndElement("", "", "div"));
                    context.end();
                } catch (XMLStreamException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            });
        } else {
            writer.add(factory.createEndElement("", "", "div"));
            context.end();
        }
    }

    protected void encodeProperty(EncodingContext<EncoderState> context) throws Exception {
        Resource resource = (Resource) context.object();
        XMLEventWriter writer = context.attachment().writer;
        XMLEventFactory factory = context.attachment().factory;

        if ( context.depth() == 0 ) {
            writer.add(factory.createStartElement("", "", "table"));
            writer.add(factory.createStartElement("", "", "tr"));

            writer.add(factory.createStartElement("", "", "th"));
            writer.add(factory.createCharacters("key"));
            writer.add(factory.createEndElement("", "", "th"));

            writer.add(factory.createStartElement("", "", "th"));
            writer.add(factory.createCharacters("value"));
            writer.add(factory.createEndElement("", "", "th"));

            writer.add(factory.createEndElement("", "", "tr"));
        }

        writer.add(factory.createStartElement("", "", "tr"));
        writer.add(factory.createStartElement("", "", "td"));
        writer.add(factory.createStartElement("", "", "a"));
        writer.add(factory.createAttribute("href", resource.uri().toString()));
        writer.add(factory.createCharacters(resource.id()));
        writer.add(factory.createEndElement("", "", "a"));
        writer.add(factory.createEndElement("", "", "td"));


        writer.add(factory.createStartElement("", "", "td"));
        context.encodeContent(() -> {
            try {
                writer.add(factory.createEndElement("", "", "td"));
                writer.add(factory.createEndElement("", "", "tr"));

                if ( context.depth() == 0 ) {
                    writer.add(factory.createEndElement("", "", "table"));
                }
                context.end();
            } catch (XMLStreamException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        });
    }

    protected void encodeValue(EncodingContext<EncoderState> context) throws Exception {
        Object value = context.object();
        XMLEventWriter writer = context.attachment().writer;
        XMLEventFactory factory = context.attachment().factory;

        writer.add(factory.createCharacters("" + value));
        context.end();
    }

    protected void encodeBinary(EncodingContext<EncoderState> context) throws Exception {
        Resource resource = (Resource) context.object();
        XMLEventWriter writer = context.attachment().writer;
        XMLEventFactory factory = context.attachment().factory;
        writer.add(factory.createStartElement("", "", "div"));

        writer.add(factory.createStartElement("", "", "div"));

        if (context.depth() == 0 && resource.parent() != null) {
            writer.add(factory.createStartElement("", "", "a"));
            writer.add(factory.createAttribute("href", resource.parent().uri().toString()));
            writer.add(factory.createCharacters(resource.parent().id()));
            writer.add(factory.createEndElement("", "", "a"));
            writer.add(factory.createCharacters(" : "));
        }

        writer.add(factory.createStartElement("", "", "b"));
        writer.add(factory.createStartElement("", "", "a"));
        writer.add(factory.createAttribute("href", resource.uri().toString()));
        writer.add(factory.createCharacters(resource.id()));
        writer.add(factory.createEndElement("", "", "b"));
        writer.add(factory.createEndElement("", "", "div"));
        writer.add(factory.createEndElement("", "", "div"));
        context.end();

    }


}
