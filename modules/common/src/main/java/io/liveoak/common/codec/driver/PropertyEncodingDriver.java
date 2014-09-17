/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec.driver;

import java.util.Properties;

import io.liveoak.spi.ReturnFields;

/**
 * @author Bob McWhirter
 */
public class PropertyEncodingDriver extends AbstractEncodingDriver {

    public PropertyEncodingDriver(EncodingDriver parent, String name, ReturnFields returnFields, Properties props) {
        super(parent, null, returnFields, props);
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

    private String name;
}
