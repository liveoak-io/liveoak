package io.liveoak.container.zero;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.container.tenancy.SimpleResourceRegistry;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

import java.util.Iterator;

/**
 * @author Bob McWhirter
 */
public class ApplicationExtensionResource extends SimpleResourceRegistry implements BlockingResource {

    private final ServiceContainer serviceContainer;
    private final ServiceName configName;

    public ApplicationExtensionResource(InternalApplicationExtension ext, ServiceContainer serviceContainer, ServiceName configName) {
        super( ext.id() );
        this.ext = ext;
        this.serviceContainer = serviceContainer;
        this.configName = configName;
    }

    @Override
    public String id() {
        return this.ext.id();
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
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.ext.remove();
    }

    @Override
    public void properties(ResourceState props) throws Exception {
        ServiceController<?> controller = this.serviceContainer.getService(this.configName);
        controller.setMode(ServiceController.Mode.REMOVE);
        this.serviceContainer.awaitStability();
        controller = this.serviceContainer.addService( this.configName, new ValueService<ObjectNode>( new ImmediateValue<>( convert( props ) )))
                .install();
        controller.awaitValue();
    }

    @Override
    public ResourceState properties() throws Exception {
        ServiceController<ObjectNode> configController = (ServiceController<ObjectNode>) this.serviceContainer.getService( this.configName );
        ObjectNode config = configController.awaitValue();
        return convert( config );
    }

    ResourceState convert(ObjectNode src) {
        ResourceState dest = new DefaultResourceState();
        Iterator<String> fieldIter = src.fieldNames();

        while ( fieldIter.hasNext() ) {
            copy( src, dest, fieldIter.next() );
        }

        return dest;
    }

    ObjectNode convert(ResourceState src) {
        ObjectNode dest = JsonNodeFactory.instance.objectNode();
        for ( String name : src.getPropertyNames() ) {
            copy( src, dest, name );
        }
        return dest;
    }

    void copy(ResourceState src, ObjectNode dest, String name) {
        Object value = src.getProperty(name);
        if ( value instanceof String ) {
            dest.put( name, JsonNodeFactory.instance.textNode((String) value) );
        } else if ( value instanceof Integer ) {
            dest.put( name, JsonNodeFactory.instance.numberNode( (Integer) value ) );
        } else if ( value instanceof Double ) {
            dest.put( name, JsonNodeFactory.instance.numberNode( (Double) value ));
        } else if ( value instanceof ResourceState ) {
            dest.put( name, convert((ResourceState) value) );
        }
    }

    void copy(ObjectNode src, ResourceState dest, String name) {
        JsonNode value = src.get( name );

        if ( value.getNodeType() == JsonNodeType.STRING ) {
            dest.putProperty( name, value.asText() );
        } else if ( value.getNodeType() == JsonNodeType.NUMBER ) {
            if ( value.numberType() == JsonParser.NumberType.INT ) {
                dest.putProperty( name, value.asInt() );
            } else if ( value.numberType() == JsonParser.NumberType.DOUBLE ) {
                dest.putProperty( name, value.asDouble() );
            }
        } else if ( value instanceof ObjectNode ) {
            dest.putProperty( name, convert( (ObjectNode) value ));
        }
    }


    private final InternalApplicationExtension ext;
    private Resource parent;

}
