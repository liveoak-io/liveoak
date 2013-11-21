/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;


/**
 * Pagination information for retrieving a subset of a collection.
 *
 * @author Bob McWhirter
 */
public interface Pagination {
    // TODO: readMember limits from configuration i.e. move this somewhere else
    static final int DEFAULT_LIMIT = 100;

    static final int MAX_LIMIT = 10000;

    static final Pagination NONE = new Pagination() {
        public int offset() {
            return 0;
        }

        public int limit() {
            return DEFAULT_LIMIT;
        }
    };

    /**
     * Get the offset.
     *
     * <p>
     * Offsets are zero-indexed.
     * </p>
     *
     * @return The offset.
     */
    int offset();

    /**
     * Get the number of items to return.
     *
     * <p>
     * A limit of less-than-zero indicates no limit.
     * </p>
     *
     * @return The limit.
     */
    int limit();

}
