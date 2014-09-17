/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec.driver;

import io.liveoak.common.codec.Encoder;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ReturnFields;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public abstract class AbstractEncodingDriver implements EncodingDriver {

    public AbstractEncodingDriver(Object object, ReturnFields returnFields) {
        this(null, object, returnFields, null);
    }

    public AbstractEncodingDriver(EncodingDriver parent, Object object, ReturnFields returnFields, Properties properties) {
        this.parent = parent;
        this.object = object;
        this.returnFields = returnFields;
        this.props = properties;

        // Perform string replace if property found
        if (properties != null && this.object != null) {
            List<Object> keys = new ArrayList<>();

            // Find keys that contain possible value matches
            properties.forEach((key, value) -> {
                if (this.object.toString().contains(value.toString())) {
                    keys.add(key);
                }
            });

            if (keys.size() == 1) {
                Object key = keys.get(0);
                this.object = this.object.toString().replace(properties.get(key).toString(), "${" + key + "}");
            } else if (keys.size() > 1) {
                Object currentKey = null;
                int currentValueSize = 0;

                for (Object key : keys) {
                    int valueSize = properties.get(key).toString().length();
                    if (valueSize > currentValueSize) {
                        currentKey = key;
                        currentValueSize = valueSize;
                    }
                }

                // Replace the env var that has the largest matching value, as this likely supersedes all others
                keys.remove(currentKey);
                this.object = this.object.toString().replace(properties.get(currentKey).toString(), "${" + currentKey + "}");

                keys.forEach(key -> this.object.toString().replace(properties.get(key).toString(), "${" + key + "}"));
            }
        }
    }

    @Override
    public Encoder encoder() {
        if (this.parent != null) {
            return this.parent.encoder();
        }
        return null;
    }

    @Override
    public RequestContext requestContext() {
        if (this.parent != null) {
            return this.parent.requestContext();
        }
        return null;
    }

    protected ReturnFields returnFields() {
        return this.returnFields;
    }

    @Override
    public Object object() {
        return this.object;
    }

    @Override
    public EncodingDriver parent() {
        return this.parent;
    }

    @Override
    public void error(Throwable t) {
        parent().error(t);
    }

    @Override
    public Properties environmentProperties() {
        return this.props;
    }

    public void encodeNext() throws Exception {
        if (children.isEmpty()) {
            close();
        } else {
            EncodingDriver next = children.removeFirst();
            next.encode();
        }
    }

    void addChildDriver(EncodingDriver child) {
        this.children.add(child);
    }

    private EncodingDriver parent;
    private Object object;
    private ReturnFields returnFields;
    private Properties props;

    private LinkedList<EncodingDriver> children = new LinkedList<>();

}
