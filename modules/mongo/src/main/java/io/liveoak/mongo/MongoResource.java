/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class MongoResource implements Resource, BlockingResource {

    protected MongoResource parent;

    protected static final String MONGO_ID_FIELD = "_id";
    protected static final String MBAAS_ID_FIELD = "id";

    public MongoResource(MongoResource parent) {
        this.parent = parent;
    }

    abstract protected Object deleteChild(RequestContext ctx, String childId);

    abstract protected Object updateChild(RequestContext ctx, String childId, Object child);

    @Override
    public Resource parent() {
        return this.parent;
    }
}
