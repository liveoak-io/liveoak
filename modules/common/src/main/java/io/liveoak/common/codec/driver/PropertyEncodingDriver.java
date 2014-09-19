/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec.driver;

import java.util.function.BiFunction;

import io.liveoak.spi.ReturnFields;

/**
 * @author Bob McWhirter
 */
public class PropertyEncodingDriver extends AbstractEncodingDriver {

    public PropertyEncodingDriver(EncodingDriver parent, String name, ReturnFields returnFields, BiFunction<String[], Object, Object> configReplaceFunction) {
        super(parent, null, returnFields, configReplaceFunction);
        this.name = name;
    }

    @Override
    public void encode() throws Exception {
        encoder().startProperty(this.name);
        encodeNext();
    }

    @Override
    public void close() throws Exception {
        encoder().endProperty(this.name);
        parent().encodeNext();
    }

    public String name() {
        return this.name;
    }

    private String name;
}
