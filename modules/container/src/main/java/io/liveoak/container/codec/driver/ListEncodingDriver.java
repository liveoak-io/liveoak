/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec.driver;

import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.resource.async.Resource;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class ListEncodingDriver extends AbstractEncodingDriver {

    public ListEncodingDriver(EncodingDriver parent, Stream<Object> object, ReturnFields returnFields) {
        super(parent, object, returnFields );
    }

    @Override
    public void encode() throws Exception {
        encoder().startList();
        ((Stream<Object>)object()).forEach( (e)->{
            if ( e instanceof Resource) {
                if ( returnFields().isEmpty() ) {
                    addChildDriver( new ValueEncodingDriver( this, e ) );
                } else {
                    addChildDriver( new ResourceEncodingDriver( this, (Resource) e, returnFields() ) );
                }
            } else if ( e instanceof List || e instanceof Set) {
                addChildDriver( new ListEncodingDriver( this, ((Collection)e).stream(), returnFields() ) );
            } else {
                addChildDriver( new ValueEncodingDriver( this, e ) );
            }
        });
        encodeNext();
    }

    @Override
    public void close() throws Exception {
        encoder().endList();
        parent().encodeNext();
    }
}
