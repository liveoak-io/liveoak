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
public interface ResourceSink extends Consumer<Resource> {

    /**
     * Complete request processing.
     *
     * <p>This method has to be called exactly once, right before a Resource method exits.
     * try-catch-finally should be used to make sure the call is performed.
     *
     * <pre>
     *   try {
     *       ...
     *       sink.accept(member);
     *       ...
     *   } catch (Throwable t) {
     *       sink.error(t);
     *   } finally {
     *       sink.close();
     *   }
     * </pre>
     *
     * <p>If {@link @error(Throwable} was called then this method will complete
     * request processing by performing container defined error handling, rather than
     * delegating processing to member resources.
     *
     * <p>Any subsequent calls to {@link #accept(Object)} or {@link #error(Throwable)}
     * will have no effect.
     *
     * <p>When this method returns request processing should be considered complete.
     */
    void complete();

    /**
     * Report an error during processing.
     *
     * <p>When {@link #complete()} is called normal processing will be skipped,
     * and error response will be composed instead.
     */
    void error(Throwable th);
}
