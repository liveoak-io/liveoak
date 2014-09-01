package io.liveoak.wildfly;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import static org.jboss.as.controller.PersistentResourceXMLDescription.builder;

/**
 * @author Bob McWhirter
 */
public class LiveOakSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    protected static LiveOakSubsystemParser INSTANCE = new LiveOakSubsystemParser();

    private static final PersistentResourceXMLDescription xmlDescription;

    static {
        xmlDescription = builder(LiveOakRootDefinition.INSTANCE)
                .addAttribute(LiveOakRootDefinition.SOCKET_BINDING)
                .build();
    }


    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        xmlDescription.parse(reader, PathAddress.EMPTY_ADDRESS, list);
    }

    @Override
    public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext ctx) throws XMLStreamException {
        ModelNode model = new ModelNode().set(LiveOakRootDefinition.INSTANCE.getPathElement().getValue(), ctx.getModelNode());
        model = new ModelNode().set(LiveOakRootDefinition.INSTANCE.getPathElement().getKey(), model);

        //model.get(UndertowRootDefinition.INSTANCE.getPathElement().getKeyValuePair()).set(context.getModelNode());//this is bit of workaround for SPRD to work properly
        xmlDescription.persist(writer, model, LiveOakExtension.NAMESPACE);

    }
}
