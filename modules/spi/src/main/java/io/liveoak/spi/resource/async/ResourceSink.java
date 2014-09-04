/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.resource.async;

import java.util.function.Consumer;

/**
 * A sink to accept children of a resource when reading.
 *
 * @author Bob McWhirter
 */
public interface ResourceSink extends Consumer<Resource>, AutoCloseable {

    void close();
}
