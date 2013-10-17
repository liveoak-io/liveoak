package org.projectodd.restafari.spi;

import java.util.Iterator;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public interface ReturnFields extends Iterable<String> {

    /**
     * Get ReturnFields for a child field of JSONObject type.
     *
     * For basic-typed fields this always returns null. Use included() for those.
     *
     * @return ReturnFields for a child field
     */
    ReturnFields child(String field);

    /**
     * Check to see if the field should be included in JSON response.
     *
     * The check can be performed for any level of depth relative to current nesting level, by specifying multiple path segments.
     *
     * @return true if the specified path should be part of JSON response or not
     */
    boolean included(String... pathSegments);

    /**
     * Iterate over child fields to be included in response.
     *
     * To get nested field specifier use child(name) passing the field name this iterator returns.
     *
     * @return iterator over child fields to be included in response.
     */
    Iterator<String> iterator();
}
