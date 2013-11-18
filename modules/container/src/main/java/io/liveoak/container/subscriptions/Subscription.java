/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.subscriptions;

import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public interface Subscription {

    String id();

    ResourcePath resourcePath();

    void resourceCreated( Resource resource ) throws Exception;

    void resourceUpdated( Resource resource ) throws Exception;

    void resourceDeleted( Resource resource ) throws Exception;

}
