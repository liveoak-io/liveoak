/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec.driver;

import java.util.function.BiFunction;

import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class ResourceEncodingDriver extends AbstractEncodingDriver {

    public ResourceEncodingDriver(Resource resource, ReturnFields returnFields) {
        super(resource, returnFields);
    }

    public ResourceEncodingDriver(EncodingDriver parent, Resource resource, ReturnFields returnFields, BiFunction<String[], Object, Object> configReplaceFunction) {
        super(parent, resource, returnFields, configReplaceFunction);
    }

    public Resource resource() {
        return (Resource) object();
    }

    @Override
    public void encode() throws Exception {
        encoder().startResource(resource());
        addChildDriver(new PropertiesEncodingDriver(this, resource(), returnFields(), replaceConfigFunction()));
        addChildDriver(new MembersEncodingDriver(this, resource(), returnFields(), replaceConfigFunction()));
        encodeNext();
    }

    @Override
    public void close() throws Exception {
        encoder().endResource(resource());
        parent().encodeNext();
    }
}
