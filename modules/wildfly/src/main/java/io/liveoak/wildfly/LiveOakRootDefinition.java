package io.liveoak.wildfly;

import org.jboss.as.controller.*;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Bob McWhirter
 */
public class LiveOakRootDefinition extends PersistentResourceDefinition {

    public static final LiveOakRootDefinition INSTANCE = new LiveOakRootDefinition();

    protected static final SimpleAttributeDefinition SOCKET_BINDING =
            new SimpleAttributeDefinitionBuilder("socket-binding", ModelType.STRING, false)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode("liveoak"))
                    .build();

    private LiveOakRootDefinition() {
        super(LiveOakExtension.SUBSYSTEM_PATH,
                LiveOakExtension.getResolver(),
                LiveOakSubsystemAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Collections.emptyList();
    }

}
