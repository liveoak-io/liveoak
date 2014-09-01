package io.liveoak.wildfly;

import java.util.Collection;
import java.util.Collections;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author Bob McWhirter
 */
public class LiveOakRootDefinition extends PersistentResourceDefinition {

    public static final String DEFAULT_SOCKET_BINDING = "liveoak";
    public static final LiveOakRootDefinition INSTANCE = new LiveOakRootDefinition();

    protected static final SimpleAttributeDefinition SOCKET_BINDING =
            new SimpleAttributeDefinitionBuilder("socket-binding", ModelType.STRING, false)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(DEFAULT_SOCKET_BINDING))
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
