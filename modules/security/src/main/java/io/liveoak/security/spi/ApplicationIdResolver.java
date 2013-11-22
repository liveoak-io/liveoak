/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.spi;

import io.liveoak.spi.RequestContext;

/**
 * Temporary class to resolve applicationId from ResourceRequest until it's clear how to obtain applicationId from request. TODO: Probably remove...
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@FunctionalInterface
public interface ApplicationIdResolver {

    String resolveAppId(RequestContext req);
}
