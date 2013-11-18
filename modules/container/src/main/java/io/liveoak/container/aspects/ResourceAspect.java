/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.aspects;

import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public interface ResourceAspect {

    Resource forResource( Resource resource );
}
