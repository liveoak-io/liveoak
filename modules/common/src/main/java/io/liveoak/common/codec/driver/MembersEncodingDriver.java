/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec.driver;

import java.util.Properties;

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

    public MembersEncodingDriver(EncodingDriver parent, Resource resource, ReturnFields returnFields, Properties props) {
        super(parent, resource, returnFields, props);
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
                    log.error("", e);
                }
                hasMembers = true;
            }
            if (resource instanceof StatusResource) {
                addChildDriver(new ResourceEncodingDriver(MembersEncodingDriver.this, resource, ReturnFields.ALL, environmentProperties()));
            } else if (returnFields().child(LiveOak.MEMBERS).isEmpty()) {
                addChildDriver(new ValueEncodingDriver(MembersEncodingDriver.this, resource, environmentProperties()));
            } else {
                addChildDriver(new ResourceEncodingDriver(MembersEncodingDriver.this, resource, returnFields().child(LiveOak.MEMBERS), environmentProperties()));
            }
        }

        @Override
        public void close() {
            try {
                encodeNext();
            } catch (Exception e) {
                error(e);
            }
        }
    }

    private boolean hasMembers;

    private static final Logger log = Logger.getLogger(MembersEncodingDriver.class);
}
