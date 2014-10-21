/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.git;

import io.liveoak.spi.resource.async.Resource;
import org.eclipse.jgit.api.Git;

/**
 * @author Ken Finnigan
 */
public interface GitResource extends Resource {
    Git git();
}
