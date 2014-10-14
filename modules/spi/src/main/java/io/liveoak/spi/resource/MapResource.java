/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.resource;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MapResource implements SynchronousResource {

    private Map<String, Object> map = new LinkedHashMap<>();

    @Override
    public Resource parent() {
        return null;
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public URI uri() {
        return null;
    }

    public MapResource put(String key, Object value) {
        map.put(key, value);
        return this;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        return map;
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        responder.updateNotSupported(this);
    }
}
