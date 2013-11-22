/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public interface ResourceParams {

    public static final ResourceParams NONE = new ResourceParams() {
        @Override
        public Collection<String> names() {
            return Collections.emptyList();
        }

        @Override
        public boolean contains(String name) {
            return false;
        }

        @Override
        public String value(String name) {
            return null;
        }

        @Override
        public List<String> values(String name) {
            return null;
        }

        @Override
        public int intValue(String name, int defaultValue) {
            return defaultValue;
        }
    };


    Collection<String> names();

    boolean contains(String name);

    String value(String name);

    List<String> values(String name);

    int intValue(String name, int defaultValue);
}
