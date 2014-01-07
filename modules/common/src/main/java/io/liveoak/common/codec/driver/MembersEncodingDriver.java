/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec.driver;

import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class MembersEncodingDriver extends ResourceEncodingDriver {

    public MembersEncodingDriver(EncodingDriver parent, Resource resource, ReturnFields returnFields) {
        super(parent, resource, returnFields);
    }

    @Override
    public void encode() throws Exception {
        resource().readMembers(requestContext(), new MyResourceSink());
    }

    @Override
    public void close() throws Exception {
        if (hasMembers) {
            encoder().endMembers();
        }
        parent().encodeNext();
    }

    private class MyResourceSink implements ResourceSink {

        @Override
        public void accept(Resource resource) {
            if (!returnFields().included("members")) {
                return;
            }
            if (!hasMembers) {
                try {
                    encoder().startMembers();
                } catch (Exception e) {
                    log.error("", e);
                }
                hasMembers = true;
            }
            if (returnFields().child("members").isEmpty()) {
                addChildDriver(new ValueEncodingDriver(MembersEncodingDriver.this, resource));
            } else {
                addChildDriver(new ResourceEncodingDriver(MembersEncodingDriver.this, resource, returnFields().child("members")));
            }
        }

        @Override
        public void close() {
            //TODO: remove the try catch here and throw an exception instead
            try {
                encodeNext();
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    private boolean hasMembers;

    private static final Logger log = Logger.getLogger(MembersEncodingDriver.class);
}
