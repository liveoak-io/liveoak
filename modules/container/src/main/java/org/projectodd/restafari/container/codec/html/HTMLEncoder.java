package org.projectodd.restafari.container.codec.html;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.container.codec.EncodingContext;
import org.projectodd.restafari.container.codec.ExpansionControllingEncoder;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.BinaryResource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;
import org.projectodd.restafari.spi.resource.async.PropertyResource;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class HTMLEncoder implements ExpansionControllingEncoder<HTMLEncoder.EncoderState> {

    public static class EncoderState {
        public XMLEventWriter writer;
        public XMLEventFactory factory;
    }

    public HTMLEncoder(DefaultContainer container) {
        this.container = container;
    }

    @Override
    public EncoderState createAttachment(ByteBuf output) throws Exception {
        EncoderState state = new EncoderState();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        state.writer = factory.createXMLEventWriter(new ByteBufOutputStream(output));
        state.factory = XMLEventFactory.newFactory();

        state.writer.add(state.factory.createStartDocument());
        state.writer.add(state.factory.createStartElement("", "", "head"));

        state.writer.add(state.factory.createStartElement("", "", "link"));
        state.writer.add(state.factory.createAttribute("rel", "stylesheet"));
        state.writer.add(state.factory.createAttribute("type", "text/css"));
        state.writer.add(state.factory.createAttribute("href", "//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css"));
        state.writer.add(state.factory.createEndElement("", "", "link"));

        state.writer.add(state.factory.createStartElement("", "", "link"));
        state.writer.add(state.factory.createAttribute("rel", "stylesheet"));
        state.writer.add(state.factory.createAttribute("type", "text/css"));
        state.writer.add(state.factory.createAttribute("href", "/css/mboss.css"));
        state.writer.add(state.factory.createEndElement("", "", "link"));

        state.writer.add(state.factory.createEndElement("", "", "head"));
        return state;
    }

    @Override
    public void close(EncodingContext<EncoderState> context) throws Exception {
        EncoderState state = context.attachment();
        XMLEventWriter writer = context.attachment().writer;
        XMLEventFactory factory = context.attachment().factory;

        writer.add(factory.createStartElement("", "", "div"));
        writer.add(factory.createAttribute("class", "footer"));
        writer.add(factory.createCharacters("mBoss HTML resource browser"));
        writer.add(factory.createEndElement("", "", "div"));
        writer.add(factory.createEndDocument());

        state.writer.flush();
        state.writer.close();
    }

    @Override
    public boolean shouldEncodeContent(EncodingContext<HTMLEncoder.EncoderState> context) {
        return (context.object() instanceof PropertyResource && context.depth() <= 1) || context.depth() == 0;
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

    protected void resourceLink(EncodingContext<EncoderState> context) throws XMLStreamException {
        Resource resource = (Resource) context.object();
        XMLEventWriter writer = context.attachment().writer;
        XMLEventFactory factory = context.attachment().factory;

        List<Resource> lineage = new ArrayList<>();

        Resource current = resource;

        if (context.depth() > 0) {
            lineage.add(current);
        } else {
            while (current != null) {
                lineage.add(0, current);
                current = current.parent();
            }
            if (lineage.get(0) != this.container) {
                lineage.add(0, this.container);
            }
        }

        for (Resource each : lineage) {
            writer.add(factory.createStartElement("", "", "a"));
            writer.add(factory.createAttribute("href", each.uri().toString()));

            String id = each.id();
            if ("".equals(id)) {
                id = "ROOT";
            }

            writer.add(factory.createCharacters(id));
            writer.add(factory.createEndElement("", "", "a"));
            if (context.depth() == 0) {
                writer.add(factory.createCharacters("/"));
            }
        }

    }

    protected void encodeCollection(EncodingContext<EncoderState> context) throws Exception {
        Resource resource = (Resource) context.object();

        XMLEventWriter writer = context.attachment().writer;
        XMLEventFactory factory = context.attachment().factory;

        writer.add(factory.createStartElement("", "", "div"));
        writer.add(factory.createAttribute("class", "collection resource"));

        writer.add(factory.createStartElement("", "", "div"));
        writer.add(factory.createAttribute("class", "self"));

        resourceLink(context);

        writer.add(factory.createEndElement("", "", "div"));

        encodeAspects( context );

        if (context.shouldEncodeContent()) {
            writer.add(factory.createStartElement("", "", "div"));
            writer.add(factory.createAttribute("class", "content"));
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
        writer.add(factory.createAttribute("class", "object resource"));

        writer.add(factory.createStartElement("", "", "div"));
        writer.add(factory.createAttribute("class", "self"));

        resourceLink(context);

        writer.add(factory.createEndElement("", "", "div"));

        encodeAspects( context );

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

    protected void encodeAspects(EncodingContext<EncoderState> context) throws Exception {
        if ( ! context.shouldEncodeContent() ) {
            return;
        }

        Resource resource = (Resource) context.object();
        final XMLEventWriter writer = context.attachment().writer;
        final XMLEventFactory factory = context.attachment().factory;

        if ( context.hasAspects() ) {
            writer.add( factory.createStartElement( "", "", "table" ) );
            writer.add( factory.createStartElement( "", "", "tr" ) );

            writer.add( factory.createStartElement( "", "", "th" ) );
            writer.add( factory.createCharacters( "Aspect" ) );
            writer.add( factory.createEndElement( "", "", "th" ) );

            writer.add( factory.createStartElement( "", "", "th" ) );
            writer.add( factory.createCharacters("Link") );
            writer.add( factory.createEndElement("", "", "th") );

            writer.add( factory.createEndElement("", "", "tr") );

            context.encodeAspects( ()->{
                try {
                    writer.add( factory.createEndElement("", "", "table") );
                } catch (XMLStreamException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            });
        }

    }

    protected void encodeProperty(EncodingContext<EncoderState> context) throws Exception {
        Resource resource = (Resource) context.object();
        XMLEventWriter writer = context.attachment().writer;
        XMLEventFactory factory = context.attachment().factory;

        if (context.depth() == 0) {
            writer.add(factory.createStartElement("", "", "div"));
            writer.add(factory.createAttribute("class", "property resource"));

            writer.add(factory.createStartElement("", "", "div"));
            writer.add(factory.createAttribute("class", "self"));

            resourceLink(context);

            writer.add(factory.createEndElement("", "", "div"));

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

                if (context.depth() == 0) {
                    writer.add(factory.createEndElement("", "", "table"));
                    writer.add(factory.createEndElement("", "", "div"));
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
        writer.add(factory.createAttribute("class", "binary resource"));

        writer.add(factory.createStartElement("", "", "div"));
        writer.add(factory.createAttribute("class", "self"));

        resourceLink(context);

        writer.add(factory.createEndElement("", "", "div"));
        writer.add(factory.createEndElement("", "", "div"));
        context.end();

    }

    private DefaultContainer container;


}
