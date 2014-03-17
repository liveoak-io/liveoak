package io.liveoak.container.tenancy.service;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.tenancy.InternalApplication;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class ApplicationResourcesService implements Service<Void> {

    public ApplicationResourcesService(JsonNode resourcesTree) {
        this.resourcesTree = resourcesTree;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if (this.resourcesTree == null) {
            return;
        }

        try {
            Iterator<String> fields = this.resourcesTree.fieldNames();
            while (fields.hasNext()) {
                String resourceId = fields.next();
                JsonNode value = this.resourcesTree.get(resourceId);
                String extensionId = value.get("type").asText();
                ObjectNode config = (ObjectNode) value.get("config");
                if (config == null) {
                    config = JsonNodeFactory.instance.objectNode();
                }
                this.applicationInjector.getValue().extend(extensionId, resourceId, config);
            }
        } catch (Exception e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<InternalApplication> applicationInjector() {
        return this.applicationInjector;
    }

    private JsonNode resourcesTree;
    private InjectedValue<InternalApplication> applicationInjector = new InjectedValue<>();
}
