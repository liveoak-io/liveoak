/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.container;

import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.SecurityContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public interface Subscription {

    String id();

    ResourcePath resourcePath();

    /**
     *
     * @return true if this subscription should be secured. If it's false, then it means that informations received by this subscription
     * are kind of public and don't need protection.
     */
    boolean isSecure();

    /**
     * @return identity of subscriber (authenticated user, who created and registered this subscription)
     */
    SecurityContext securityContext();

    /**
     * Send authorization error in case that subscriber is not authorized to receive subscriptions message about particular resource
     *
     * @param errorState state, which will be sent to user (Error message etc)
     * @param resource resource, which is user not allowed to see
     * @param status error code (For example 403 if user is not authorized or 401 if he is not authenticated at all)
     * @throws Exception if message couldn't be sent
     */
    void sendAuthzError(ResourceState errorState, Resource resource, int status) throws Exception;

    void resourceCreated(ResourceResponse resourceResponse) throws Exception;

    void resourceUpdated(ResourceResponse resourceResponse) throws Exception;

    void resourceDeleted(ResourceResponse resourceResponse) throws Exception;

}
