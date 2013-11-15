/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem;

import io.liveoak.spi.resource.async.Resource;
import org.vertx.java.core.Vertx;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public interface FSResource extends Resource {
    Vertx vertx();

    File file();
}
