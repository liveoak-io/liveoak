package io.liveoak.container.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.state.ResourceState;

import java.util.Iterator;

/**
 * @author Bob McWhirter
 */
public class ConversionUtils {

    public static ResourceState convert(ObjectNode src) {
        ResourceState dest = new DefaultResourceState();
        Iterator<String> fieldIter = src.fieldNames();

        while ( fieldIter.hasNext() ) {
            copy( src, dest, fieldIter.next() );
        }

        return dest;
    }

    public static ObjectNode convert(ResourceState src) {
        ObjectNode dest = JsonNodeFactory.instance.objectNode();
        for ( String name : src.getPropertyNames() ) {
            copy( src, dest, name );
        }
        return dest;
    }

    private static void copy(ResourceState src, ObjectNode dest, String name) {
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

    private static void copy(ObjectNode src, ResourceState dest, String name) {
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
}
