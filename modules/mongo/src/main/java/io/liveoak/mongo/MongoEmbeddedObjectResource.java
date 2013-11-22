/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.mongo;

import com.mongodb.DBObject;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.net.URI;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoEmbeddedObjectResource extends MongoObjectResource {

    public MongoEmbeddedObjectResource(MongoResource parent, DBObject dbObject) {
        super(parent, dbObject);
    }

    // Embedded Mongo Resources do not have ids and are not URL addressable.
    @Override
    public String id() {
        return null;
    }

    @Override
    public URI uri() {
        return null;
    }

    // Embedded Mongo Resources are read only. If you want to update an embedded resource, you need to do so on the base resource level
    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) {
        responder.noSuchResource(null);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        responder.noSuchResource(null);
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) {
        responder.noSuchResource(null);
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        responder.noSuchResource(null);
    }

}
