package io.liveoak.container.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.tenancy.InternalApplication;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class CommonExtensions extends AbstractServiceListener {

    public CommonExtensions() {

    }

    public void register(String extensionId, ObjectNode appConfig) {
        this.extensions.put(extensionId, appConfig);
    }

    public void unregister(String extensionId) {
        this.extensions.remove(extensionId);
    }

    public void extend(InternalApplication app) {
        for ( Map.Entry<String, ObjectNode> entry : this.extensions.entrySet() ) {
            try {
                app.extend( entry.getKey(), entry.getValue() );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void transition(ServiceController controller, ServiceController.Transition transition) {
        if (transition.getAfter().equals(ServiceController.Substate.UP)) {
            Object service = controller.getService();
            if (service instanceof ExtensionService) {
                ExtensionService ext = (ExtensionService) service;
                if ( ext.common() ) {
                    register( ext.id(), ext.applicationConfiguration() );
                }
            }
        } else if (transition.getBefore().equals(ServiceController.Substate.UP)) {
            Object service = controller.getService();
            if (service instanceof ExtensionService) {
                if (((ExtensionService) service).common()) {
                    unregister(((ExtensionService) service).id());
                }
            }
        }
    }

    private Map<String,ObjectNode> extensions = new HashMap<>();
}
