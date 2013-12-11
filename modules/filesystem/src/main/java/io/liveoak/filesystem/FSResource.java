/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem;

import java.io.File;

import io.liveoak.spi.resource.VertxResource;

/**
 * @author Bob McWhirter
 */
public interface FSResource extends VertxResource {

    File file();

    boolean writable();
}
