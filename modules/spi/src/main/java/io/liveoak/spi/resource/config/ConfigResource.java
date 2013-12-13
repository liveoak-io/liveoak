package io.liveoak.spi.resource.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
        Method[] methods = resource.getClass().getDeclaredMethods();

        // Check fields
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ConfigProperty configProperty = field.getAnnotation(ConfigProperty.class);

                if (configProperty != null) {
                    String key = field.getName();
                    if (!"".equals(configProperty.value())) {
                        key = configProperty.value();
                    }

                    // Retrieve the value of the field
                    field.setAccessible(Boolean.TRUE);
                    Object value = field.get(resource);

                    if (value != null) {
                        // Check for converter
                        if (!configProperty.converter().isInterface()) {
                            Class<? extends ConfigPropertyConverter> converterClass = configProperty.converter();
                            ConfigPropertyConverter converter = converterClass.getConstructor().newInstance();
                            value = converter.toConfigValue(value);
                        }
                    }

                    if (value != null) {
                        // Add the value to the sink if it's not null
                        sink.accept(key, value);
                    } else {
                        System.err.println("Unable to get value for key: " + key);
                    }
                }
            }
        }

        // Check methods
        if (methods != null && methods.length > 0) {
            for (Method method : methods) {
                ConfigMappingExporter configExporter = method.getAnnotation(ConfigMappingExporter.class);

                if (configExporter != null) {
                    String key = method.getName();
                    if (!"".equals(configExporter.value())) {
                        key = configExporter.value();
                    }

                    Object value = method.invoke(resource);

                    if (value != null) {
                        // Add the value to the sink if it's not null
                        sink.accept(key, value);
                    } else {
                        System.err.println("Unable to get value for key: " + key);
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
                ConfigMapping configMapping = field.getAnnotation(ConfigMapping.class);

                if (configProperty != null) {
                    // Retrieve the key for the property.
                    // Defaults to field name if annotation does not specify name.
                    String key = field.getName();
                    if (!"".equals(configProperty.value())) {
                        key = configProperty.value();
                    }

                    Object value = state.getProperty(key);

                    if (value == null) {
                        String msg = !"".equals(configProperty.msg())
                                ? configProperty.msg()
                                : "No configuration value specified for: " + key + " on Resource with id: " + resource.id();
                        throw new InitializationException(msg);
                    }

                    // Check for converter
                    if (!configProperty.converter().isInterface()) {
                        Class<? extends ConfigPropertyConverter> converterClass = configProperty.converter();
                        ConfigPropertyConverter converter = converterClass.getConstructor().newInstance();
                        value = converter.createFrom(value);
                    }

                    // Set the value on the field
                    field.setAccessible(Boolean.TRUE);
                    if (value.getClass().isAssignableFrom((Class<?>) field.getGenericType())) {
                        field.set(resource, value);
                    }

                } else if (configMapping != null) {
                    ConfigProperty[] mappingProperties = configMapping.properties();
                    String importMethod = configMapping.importMethod();
                    Object[] configValues = new Object[mappingProperties.length];
                    int count = 0;

                    // Retrieve config values from state
                    for (ConfigProperty property : mappingProperties) {
                        configValues[count++] = state.getProperty(property.value());
                    }

                    // Create object from config state
                    Method method = resource.getClass().getDeclaredMethod(importMethod, Object[].class);
                    method.setAccessible(Boolean.TRUE);
                    method.invoke(resource, new Object[] {configValues});
                }
            }
        }
    }
}
