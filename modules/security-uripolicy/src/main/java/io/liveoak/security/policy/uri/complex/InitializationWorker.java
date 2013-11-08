/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.uri.complex;

/**
 * Worker, which will perform some initialization steps. Those could be executed in dedicated initialization thread
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@FunctionalInterface
public interface InitializationWorker {

    void run(URIPolicy uriPolicy);
}
