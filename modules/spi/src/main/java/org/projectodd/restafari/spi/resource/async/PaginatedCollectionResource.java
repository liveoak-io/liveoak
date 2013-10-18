package org.projectodd.restafari.spi.resource.async;

import org.projectodd.restafari.spi.Pagination;

/** A collection resource representing a subset of a larger collection.
 *
 * @author Bob McWhirter
 */
public interface PaginatedCollectionResource extends CollectionResource {

    /** Retrieve the collection that contains this subset.
     *
     * @return The parent collection.
     */
    CollectionResource collection();

    /** Retrieve pagination information describing this subset.
     *
     * @return The pagination information.
     */
    Pagination pagination();
}
