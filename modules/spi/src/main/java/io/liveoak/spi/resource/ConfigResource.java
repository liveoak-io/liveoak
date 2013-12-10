package io.liveoak.spi.resource;

import java.lang.reflect.Field;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public interface ConfigResource extends Resource {

    @Override
    default String id() {
        return ";config";
    }

    default void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        readConfigProperties(ctx, sink, this.parent());
        sink.close();
    }

    default void readConfigProperties(RequestContext ctx, PropertySink sink, Resource resource) throws Exception {
        Field[] fields = resource.getClass().getDeclaredFields();

        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ConfigProperty configProperty = field.getAnnotation(ConfigProperty.class);

                if (configProperty != null) {
                    // Retrieve the key for the property.
                    // Defaults to field name if annotation does not specify name.
                    String key = field.getName();
                    if (!"".equals(configProperty.name())) {
                        key = configProperty.name();
                    }

                    // Retrieve the value of the field
                    field.setAccessible(true);
                    Object value = field.get(resource);

                    if (value != null) {
                        // Add the value to the sink if it's not null
                        sink.accept(key, value);
                    }
                }
            }
        }
    }

    default void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        updateConfigProperties(ctx, state, responder, this.parent());
        responder.resourceUpdated(this);
    }

    default void updateConfigProperties(RequestContext ctx, ResourceState state, Responder responder, Resource resource) throws Exception {
        Field[] fields = resource.getClass().getDeclaredFields();

        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ConfigProperty configProperty = field.getAnnotation(ConfigProperty.class);

                if (configProperty != null) {
                    // Retrieve the key for the property.
                    // Defaults to field name if annotation does not specify name.
                    String key = field.getName();
                    if (!"".equals(configProperty.name())) {
                        key = configProperty.name();
                    }

                    Object value = state.getProperty(key);

                    if (value == null) {
                        String msg = !"".equals(configProperty.msg())
                                ? configProperty.msg()
                                : "No configuration value specified for: " + key + " on Resource with id: " + resource.id();
                        throw new InitializationException(msg);
                    }

                    // Set the value on the field
                    field.setAccessible(true);
                    if (value.getClass().isAssignableFrom((Class<?>) field.getGenericType())) {
                        field.set(resource, value);
                    }
                }
            }
        }
    }
}
