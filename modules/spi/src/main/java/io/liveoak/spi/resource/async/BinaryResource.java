/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.resource.async;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;

/**
 * @author Bob McWhirter
 */
public interface BinaryResource extends Resource {

    MediaType mediaType();
    void readContent(RequestContext ctx, BinaryContentSink sink);
}
