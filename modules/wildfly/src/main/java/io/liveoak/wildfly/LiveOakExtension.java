package io.liveoak.wildfly;

import io.liveoak.spi.LiveOak;
import org.jboss.as.cli.parsing.ParserUtil;
import org.jboss.as.controller.*;
import org.jboss.as.controller.descriptions.NonResolvingResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.parsing.Element;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class LiveOakExtension implements Extension {
    /** The name space used for the {@code substystem} element */
    public static final String NAMESPACE = "urn:liveoak:liveoak:1.0";

    /** The name of our subsystem within the model. */
    public static final String SUBSYSTEM_NAME = "liveoak";

    private static final String RESOURCE_NAME = LiveOakExtension.class.getPackage().getName() + ".LocalDescriptions";

    protected static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);

    public static StandardResourceDescriptionResolver getResolver(final String... keyPrefix) {
        StringBuilder prefix = new StringBuilder(SUBSYSTEM_NAME);
        for (String kp : keyPrefix) {
            prefix.append('.').append(kp);
        }
        return new StandardResourceDescriptionResolver(prefix.toString(), RESOURCE_NAME, LiveOakExtension.class.getClassLoader(), true, false);
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, LiveOakSubsystemParser.INSTANCE);
    }

    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0, 0);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(new SimpleResourceDefinition(
                PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME),
                new NonResolvingResourceDescriptionResolver(),
                LiveOakSubsystemAdd.INSTANCE,
                LiveOakSubsystemRemove.INSTANCE
        ));
        //We always need to add a 'describe' operation
        registration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);
        subsystem.registerXMLElementWriter(LiveOakSubsystemParser.INSTANCE);
        System.err.println( "liveoak subsystem, activate!" );

    }

}
