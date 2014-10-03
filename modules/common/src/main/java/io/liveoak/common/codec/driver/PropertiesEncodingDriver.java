/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec.driver;

import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.logging.Logger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author Bob McWhirter
 */
public class PropertiesEncodingDriver extends ResourceEncodingDriver {

    public PropertiesEncodingDriver(ResourceEncodingDriver parent, Resource resource, ReturnFields returnFields, BiFunction<String[], Object, Object> configReplaceFunction) {
        super(parent, resource, returnFields, configReplaceFunction);
    }

    @Override
    public void encode() throws Exception {
        resource().readProperties(requestContext(), new MyPropertySink());
    }

    @Override
    public void close() throws Exception {
        if (this.hasProperties) {
            encoder().endProperties();
        }
        parent().encodeNext();
    }

    private class MyPropertySink implements PropertySink {

        @Override
        public void accept(String name, Object value) {
            if (!returnFields().included(name)) {
                return;
            }
            if (!hasProperties) {
                try {
                    encoder().startProperties();
                } catch (Exception e) {
                    log.error("", e);
                }
                hasProperties = true;
            }
            PropertyEncodingDriver propDriver = new PropertyEncodingDriver(PropertiesEncodingDriver.this, name, null, replaceConfigFunction());
            if (value instanceof Resource) {
                // embedded resource's don't have id's and should always be displayed unless the return field is set
                if (((Resource) value).id() == null && returnFields().child(name).isEmpty()) {
                    propDriver.addChildDriver(new ResourceEncodingDriver(propDriver, (Resource) value, returnFields().ALL, replaceConfigFunction()));
                } else if (!returnFields().child(name).isEmpty()) {
                    propDriver.addChildDriver(new ResourceEncodingDriver(propDriver, (Resource) value, returnFields().child(name), replaceConfigFunction()));
                } else {
                    propDriver.addChildDriver(new ValueEncodingDriver(propDriver, value));
                }
            } else if (value instanceof List || value instanceof Set) {
                propDriver.addChildDriver(new ListEncodingDriver(propDriver, ((Collection) value).stream(), returnFields().child(name), replaceConfigFunction()));
            } else {
                if (replaceConfigFunction() != null) {
                    // Build hierarchy of property names
                    LinkedList<String> names = new LinkedList<>();
                    names.addFirst(name);
                    EncodingDriver parent = propDriver.parent();
                    while (parent != null) {
                        if (parent instanceof PropertyEncodingDriver) {
                            names.addLast(((PropertyEncodingDriver)parent).name());
                        }
                        parent = parent.parent();
                    }
                    value = replaceConfigFunction().apply(names.toArray(new String[names.size()]), value);
                }
                propDriver.addChildDriver(new ValueEncodingDriver(propDriver, value));
            }
            addChildDriver(propDriver);
        }

        @Override
        public void error(Throwable e) {
            error = e;
        }

        @Override
        public void complete() {
            try {
                if (error == null) {
                    encodeNext();
                }
            } catch (Exception e) {
                error = e;
            }
            if (error != null) {
                PropertiesEncodingDriver.this.error(error);
                // TODO proper exception hierarchy would discern between container exceptions and resource exceptions
                // Resource exception should be mappable to response codes in straight away fashion
                if (error instanceof RuntimeException) {
                    throw (RuntimeException) error;
                } else {
                    throw new RuntimeException("Exception while processing request: ", error);
                }
            }
        }

        @Override
        public void replaceConfig(BiFunction<String[], Object, Object> function) {
            setReplaceConfigFunction(function);
        }

        private Throwable error;
    }

    private boolean hasProperties;

    private static final Logger log = Logger.getLogger(PropertiesEncodingDriver.class);

}
