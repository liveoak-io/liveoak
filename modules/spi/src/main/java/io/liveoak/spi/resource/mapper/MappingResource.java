package io.liveoak.spi.resource.mapper;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import io.liveoak.spi.exceptions.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * Provides automatic mapping between String values on {@link io.liveoak.spi.state.ResourceState} and
 * the fields, or method parameters, that represent the properties of the resource.
 *
 * Typically useful on {@link io.liveoak.spi.resource.RootResource} that contains properties.
 *
 * @author Ken Finnigan
 */
public interface MappingResource extends Resource {

    @Override
    default void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        mapPropertiesForRead(ctx, sink, this);
        sink.complete();
    }

    default void mapPropertiesForRead(RequestContext ctx, PropertySink sink, Resource resource) throws Exception {
        // Check fields
        for (Field field : getFields(resource.getClass(), Property.class)) {
            Property property = field.getAnnotation(Property.class);
            String key = field.getName();
            if (!"".equals(property.value())) {
                key = property.value();
            }

            // Retrieve the value of the field
            field.setAccessible(Boolean.TRUE);
            Object value = field.get(resource);

            if (value != null) {
                // Check for converter
                if (!property.converter().isInterface()) {
                    Class<? extends PropertyConverter> converterClass = property.converter();
                    PropertyConverter converter = converterClass.getConstructor().newInstance();
                    value = converter.toValue(value);
                } else {
                    // Convert some known types
                    if (field.getType().equals(File.class)) {
                        value = ((File)value).getAbsolutePath();
                    } else if (field.getType().equals(URL.class)) {
                        value = value.toString();
                    } else if (field.getType().equals(URI.class)) {
                        value = value.toString();
                    } else if (field.getType().equals(Float.class)) {
                        value = value.toString();
                    } else if (field.getType().equals(Short.class)) {
                        value = value.toString();
                    }
                }
            }

            if (value != null) {
                // Add the value to the sink if it's not null
                sink.accept(key, value);
            } else {
                log.warn("Unable to get value for key: " + key);
            }
        }

        // Check methods
        for (Method method : getMethods(resource.getClass(), MappingExporter.class)) {
            MappingExporter exporter = method.getAnnotation(MappingExporter.class);

            if (exporter != null) {
                HashMap<String, Object> values = new HashMap<>();
                method.invoke(resource, values);

                if (values != null && !values.isEmpty()) {
                    for (HashMap.Entry<String, Object> entry : values.entrySet()) {
                        if (entry.getValue() != null) {
                            // Add the value to the sink if it's not null
                            sink.accept(entry.getKey(), entry.getValue());
                        } else {
                            log.warn("Unable to get value for key: " + entry.getKey());
                        }
                    }
                }
            }
        }
    }

    @Override
    default void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        mapPropertiesForUpdate(ctx, state, responder, this);
        responder.resourceUpdated(this);
    }

    default void mapPropertiesForUpdate(RequestContext ctx, ResourceState state, Responder responder, Resource resource) throws Exception {
        for (Field field : getFields(resource.getClass(), Property.class)) {
            Property property = field.getAnnotation(Property.class);

            // Retrieve the key for the property.
            // Defaults to field name if annotation does not specify name.
            String key = field.getName();
            if (!"".equals(property.value())) {
                key = property.value();
            }

            Object value = state.getProperty(key);

            if (value == null) {
                String msg = !"".equals(property.msg())
                        ? property.msg()
                        : "No value specified for: " + key + " on Resource with id: " + resource.id();
                throw new InitializationException(msg);
            }

            // Check for converter
            if (!property.converter().isInterface()) {
                Class<? extends PropertyConverter> converterClass = property.converter();
                PropertyConverter converter = converterClass.getConstructor().newInstance();
                value = converter.createFrom(value);
            } else {
                // Convert some known types
                if (field.getType().equals(File.class)) {
                    value = new File(value.toString());
                } else if (field.getType().equals(Boolean.class)) {
                    value = new Boolean(value.toString());
                } else if (field.getType().equals(URL.class)) {
                    value = new URL(value.toString());
                } else if (field.getType().equals(URI.class)) {
                    value = new URI(value.toString());
                } else if (field.getType().equals(Integer.class)) {
                    value = new Integer(value.toString());
                } else if (field.getType().equals(Double.class)) {
                    value = new Double(value.toString());
                } else if (field.getType().equals(Float.class)) {
                    value = new Float(value.toString());
                } else if (field.getType().equals(Long.class)) {
                    value = new Long(value.toString());
                } else if (field.getType().equals(Short.class)) {
                    value = new Short(value.toString());
                }
            }

            // Set the value on the field
            field.setAccessible(Boolean.TRUE);
            if (value.getClass().isAssignableFrom((Class<?>) field.getGenericType())) {
                field.set(resource, value);
            }
        }

        // Check methods
        for (Method method : getMethodsWithParamAnnotation(resource.getClass(), Property.class)) {
            Parameter[] params = method.getParameters();

            Object[] values = new Object[params.length];
            int count = 0;

            // Retrieve config values from state
            for (Parameter methodParam : params) {
                Property methodParamProp = methodParam.getAnnotation(Property.class);
                String key = methodParamProp.value();
                if (key == null || "".equals(key)) {
                    throw new InitializationException("No value defined on @Property of " + methodParam.getName()
                            + " parameter on method " + method.getName() + "() in " + resource.getClass());
                }
                values[count++] = methodParam.getType().cast(state.getProperty(key));
            }

            // Create object from config state
            method.setAccessible(Boolean.TRUE);
            method.invoke(resource, values);
        }
    }

    static Iterable<Method> getMethods(Class<?> c, Class<? extends Annotation> a) {
        List<Method> methods = new LinkedList<>();
        while (c != null) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getAnnotation(a) != null) {
                    boolean added = false;

                    for (Method e : methods) {
                        if (m.getName().equals(e.getName()) && Arrays.equals(m.getParameterTypes(), e.getParameterTypes())) {
                            added = true;
                        }
                    }

                    if (!added) {
                        methods.add(m);
                    }
                }
            }
            c = c.getSuperclass();
            if (c == Object.class) {
                c = null;
            }
        }
        return methods;
    }

    static Iterable<Method> getMethodsWithParamAnnotation(Class<?> c, Class<? extends Annotation> a) {
        List<Method> methods = new LinkedList<>();
        while (c != null) {
            for (Method m : c.getDeclaredMethods()) {
                Annotation[][] methodParameterAnnotations = m.getParameterAnnotations();
                for (Annotation[] parameterAnnotations : methodParameterAnnotations) {
                    for (int i = 0; i < parameterAnnotations.length; i++) {
                        Annotation paramAnn = parameterAnnotations[i];
                        if (paramAnn.annotationType().equals(a)) {
                            boolean added = false;

                            for (Method e : methods) {
                                if (m.getName().equals(e.getName()) && Arrays.equals(m.getParameterTypes(), e.getParameterTypes())) {
                                    added = true;
                                }
                            }

                            if (!added) {
                                methods.add(m);
                            }
                            break;
                        }
                    }
                }
            }
            c = c.getSuperclass();
            if (c == Object.class) {
                c = null;
            }
        }
        return methods;
    }

    static Iterable<Field> getFields(Class<?> c, Class<? extends Annotation> a) {
        List<Field> fields = new LinkedList<>();
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getAnnotation(a) != null) {
                    boolean added = false;

                    for (Field e : fields) {
                        if (f.getName().equals(e.getName()) && f.getType().equals(e.getType())) {
                            added = true;
                        }
                    }

                    if (!added) {
                        fields.add(f);
                    }
                }
            }
            c = c.getSuperclass();
            if (c == Object.class) {
                c = null;
            }
        }
        return fields;
    }

    static final Logger log = Logger.getLogger(MappingResource.class);
}
