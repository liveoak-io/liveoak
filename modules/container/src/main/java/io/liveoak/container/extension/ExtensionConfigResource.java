package io.liveoak.container.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.util.ConversionUtils;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class ExtensionConfigResource implements RootResource, SynchronousResource {

    public ExtensionConfigResource(String id, ServiceContainer serviceContainer, ServiceName configName) {
        this.id = id;
        this.serviceContainer = serviceContainer;
        this.configName = configName;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void properties(ResourceState props) throws Exception {
        ServiceController<?> controller = this.serviceContainer.getService(this.configName);
        controller.setMode(ServiceController.Mode.REMOVE);
        this.serviceContainer.awaitStability();
        controller = this.serviceContainer.addService( this.configName, new ValueService<ObjectNode>( new ImmediateValue<>( ConversionUtils.convert(props) )))
                .install();
        controller.awaitValue();
    }

    @Override
    public ResourceState properties() throws Exception {
        ServiceController<ObjectNode> configController = (ServiceController<ObjectNode>) this.serviceContainer.getService( this.configName );
        ObjectNode config = configController.awaitValue();
        return ConversionUtils.convert(config);
    }



    private Resource parent;
    private String id;
    private final ServiceContainer serviceContainer;
    private final ServiceName configName;
}
