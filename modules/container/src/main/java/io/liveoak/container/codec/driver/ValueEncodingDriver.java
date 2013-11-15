/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec.driver;

import io.liveoak.spi.resource.async.Resource;

import java.util.Date;

/**
 * @author Bob McWhirter
 */
public class ValueEncodingDriver extends AbstractEncodingDriver {

    public ValueEncodingDriver(EncodingDriver parent, Object object) {
        super(parent, object, null);
    }

    @Override
    public void encode() throws Exception {
        Object o = object();

        if ( o instanceof String ) {
            encoder().writeValue((String) o);
        } else if ( o instanceof Integer ) {
            encoder().writeValue((Integer) o);
        } else if ( o instanceof Double ) {
            encoder().writeValue((Double) o);
        } else if ( o instanceof Date) {
            encoder().writeValue((Date) o);
        } else if ( o instanceof Resource) {
            encoder().writeLink((Resource) o);
        } else {
        }
        close();
    }

    @Override
    public void close() throws Exception {
        parent().encodeNext();
    }
}
