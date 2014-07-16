/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public interface ReturnFields extends Iterable<String> {

    static ReturnFields ALL = new ReturnFields() {
        @Override
        public ReturnFields child(String field) {
            return NONE;
        }

        @Override
        public boolean included(String... pathSegments) {
            return true;
        }

        @Override
        public Iterator<String> iterator() {
            return Collections.singletonList("*").iterator();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        public boolean isAll() {
            return true;
        }

        @Override
        public String toString() {
            return "[ReturnFields ALL]";
        }
    };

    static ReturnFields NONE = new ReturnFields() {
        @Override
        public ReturnFields child(String field) {
            return this;
        }

        @Override
        public boolean included(String... pathSegments) {
            return false;
        }

        @Override
        public Iterator<String> iterator() {
            List<String> emptyList = Collections.emptyList();
            return emptyList.iterator();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean isAll() {
            return false;
        }

        @Override
        public String toString() {
            return "[ReturnFields NONE]";
        }
    };

    static ReturnFields ALL_RECURSIVELY = new ReturnFields() {
        @Override
        public ReturnFields child(String field) {
            return this;
        }

        @Override
        public boolean included(String... pathSegments) {
            return true;
        }

        @Override
        public Iterator<String> iterator() {
            List<String> emptyList = Collections.emptyList();
            return emptyList.iterator();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean isAll() {
            return true;
        }
    };


    /**
     * Get ReturnFields for a child field of JSONObject type.
     *
     * <p>For basic-typed fields this always returns null. Use included() for those.</p>
     *
     * @param field The child field name for nested returns.
     * @return ReturnFields for a child field
     */
    ReturnFields child(String field);

    /**
     * Check to see if the field should be included in JSON response.
     *
     * <p>The check can be performed for any level of depth relative to current nesting level, by specifying multiple path segments.</p>
     *
     * @param pathSegments Segments to test in the tree of return fields.
     * @return true if the specified path should be part of JSON response or not
     */
    boolean included(String... pathSegments);

    /**
     * Iterate over child fields to be included in response.
     *
     * <p>To get nested field specifier use child(name) passing the field name this iterator returns.</p>
     *
     * @return iterator over child fields to be included in response.
     */
    Iterator<String> iterator();

    /**
     * Determine if zero fields should be returned.
     *
     * @return <code>true</code> if the list is empty, else, <code>false</code>
     */
    boolean isEmpty();

    boolean isAll();
}
