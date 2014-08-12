/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.drools.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.liveoak.spi.ResourceParams;

/**
 * Decorator of {@link ResourceParams} with some added methods to allow safe processing of request parameters by drools engine
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ResourceParamsDecorator implements ResourceParams {

    private final ResourceParams delegate;

    public ResourceParamsDecorator(ResourceParams delegate) {
        this.delegate = delegate;
    }

    @Override
    public Collection<String> names() {
        return delegate.names();
    }

    @Override
    public boolean contains(String name) {
        return delegate.contains(name);
    }

    @Override
    public String value(String name) {
        return delegate.value(name);
    }

    @Override
    public List<String> values(String name) {
        return delegate.values(name);
    }

    /**
     * return parameter value if parameter exists. Otherwise return empty string. Never returns null
     *
     * @param name
     * @return parameter value
     */
    public String safeValue(String name) {
        String val = value(name);
        return val == null ? "" : val;
    }

    /**
     * Return intValue if param exists and if param could be converted to int. Otherwise returns null. Never throws NumberFormatException
     *
     * @param name
     * @return intValue
     */
    public Integer intValue(String name) {
        String val = value(name);
        if (val == null) {
            return null;
        } else {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    /**
     * Return true only if params exists and it's value is "true" (with equals-ignore-case semantics)
     *
     * @param name
     * @return booleanValue. Never returns null
     */
    public Boolean booleanValue(String name) {
        String val = value(name);
        return Boolean.parseBoolean(val);
    }
}
