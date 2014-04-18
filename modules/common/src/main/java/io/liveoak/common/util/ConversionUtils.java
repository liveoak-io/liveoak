package io.liveoak.common.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.state.ResourceState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class ConversionUtils {

    public static ResourceState convert(ObjectNode src) {
        ResourceState dest = new DefaultResourceState();
        Iterator<String> fieldIter = src.fieldNames();

        while (fieldIter.hasNext()) {
            copy(src, dest, fieldIter.next());
        }

        return dest;
    }

    public static ObjectNode convert(ResourceState src) {
        ObjectNode dest = JsonNodeFactory.instance.objectNode();
        for (String name : src.getPropertyNames()) {
            copy(src, dest, name);
        }
        return dest;
    }

    public static JsonNode toJSON(Object value) {
        if (value instanceof String) {
            return JsonNodeFactory.instance.textNode((String) value);
        } else if (value instanceof Integer) {
            return JsonNodeFactory.instance.numberNode((Integer) value);
        } else if (value instanceof Long) {
            return JsonNodeFactory.instance.numberNode((Long) value);
        } else if (value instanceof Double) {
            return JsonNodeFactory.instance.numberNode((Double) value);
        } else if ( value instanceof Boolean ) {
            return JsonNodeFactory.instance.booleanNode( (Boolean) value );
        } else if (value instanceof ResourceState) {
            return convert((ResourceState) value);
        } else if (value instanceof List) {
            ArrayNode array = JsonNodeFactory.instance.arrayNode();
            for (Object e : (List) value) {
                array.add(toJSON(e));
            }
            return array;
        }

        return null;
    }

    public static Object toRS(JsonNode value) {
        if (value.getNodeType() == JsonNodeType.STRING) {
            return value.asText();
        } else if (value.getNodeType() == JsonNodeType.NUMBER) {
            if (value.numberType() == JsonParser.NumberType.INT) {
                return value.asInt();
            } else if (value.numberType() == JsonParser.NumberType.LONG) {
                return value.asLong();
            } else if (value.numberType() == JsonParser.NumberType.DOUBLE) {
                return value.asDouble();
            }
        } else if (value.getNodeType() == JsonNodeType.BOOLEAN) {
            return value.asBoolean();
        } else if ( value instanceof ArrayNode ) {
            List<Object> array = new ArrayList<Object>();
            value.elements().forEachRemaining( (e)->{
                array.add( toRS( e ) );
            });
            return array;
        } else if (value instanceof ObjectNode) {
            return convert( (ObjectNode) value );
        }
        return null;
    }

    private static void copy(ResourceState src, ObjectNode dest, String name) {
        Object value = src.getProperty(name);
        dest.put(name, toJSON(value));
    }

    private static void copy(ObjectNode src, ResourceState dest, String name) {
        JsonNode value = src.get(name);
        dest.putProperty( name, toRS( value ));
    }
}
