/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec.driver;

import java.util.function.BiFunction;

import io.liveoak.spi.LiveOak;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.resource.StatusResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class MembersEncodingDriver extends ResourceEncodingDriver {

    public MembersEncodingDriver(EncodingDriver parent, Resource resource, ReturnFields returnFields, BiFunction<String[], Object, Object> configReplaceFunction) {
        super(parent, resource, returnFields, configReplaceFunction);
    }

    @Override
    public void encode() throws Exception {
        //we should only read the members if they are going to be returned in the response
        //otherwise it could be an expensive operation when all we are requesting is metadata (ie count)
        if (requestContext().returnFields().included(LiveOak.MEMBERS)) {
            resource().readMembers(requestContext(), new MyResourceSink());
        } else {
            encodeNext();
        }
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
            if (!returnFields().included(LiveOak.MEMBERS)) {
                return;
            }
            if (!hasMembers) {
                try {
                    encoder().startMembers();
                } catch (Exception e) {
                    // TODO repackage into RuntimeException and rethrow?
                    // Or introduce EncoderException extends RuntimeException?
                    // Or use ResourceProcessingException and make it extend RuntimeException?
                    log.error("Encoder exception: ", e);
                }
                hasMembers = true;
            }
            if (resource instanceof StatusResource) {
                addChildDriver(new ResourceEncodingDriver(MembersEncodingDriver.this, resource, ReturnFields.ALL, replaceConfigFunction()));
            } else if (returnFields().child(LiveOak.MEMBERS).isEmpty()) {
                addChildDriver(new ValueEncodingDriver(MembersEncodingDriver.this, resource));
            } else {
                addChildDriver(new ResourceEncodingDriver(MembersEncodingDriver.this, resource, returnFields().child(LiveOak.MEMBERS), replaceConfigFunction()));
            }
        }

        @Override
        public void error(Throwable t) {
            error = t;
        }

        @Override
        public void close() {
            try {
                if (error == null) {
                    encodeNext();
                }
            } catch (Exception e) {
                error = e;
            }
            if (error != null) {
                MembersEncodingDriver.this.error(error);
            }
        }

        private Throwable error;
    }

    private boolean hasMembers;

    private static final Logger log = Logger.getLogger(MembersEncodingDriver.class);
}
