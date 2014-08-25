/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common;

import io.liveoak.spi.RequestAttributes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultRequestAttributes implements RequestAttributes {

    private Map<String, Object> attributesMap;

    public DefaultRequestAttributes() {
    }


    @Override
    public Collection<String> getAttributeNames() {
        if (attributesMap == null) {
            return null;
        } else {
            return attributesMap.keySet();
        }
    }

    @Override
    public Object getAttribute(String attributeName) {
        if (attributesMap == null) {
            return null;
        } else {
            return attributesMap.get(attributeName);
        }
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue) {
        if (attributesMap == null) {
            attributesMap = new HashMap<>();
        }
        attributesMap.put(attributeName, attributeValue);
    }

    @Override
    public Object removeAttribute(String attributeName) {
        if (attributesMap == null) {
            return null;
        } else {
            return attributesMap.remove(attributeName);
        }
    }
}
