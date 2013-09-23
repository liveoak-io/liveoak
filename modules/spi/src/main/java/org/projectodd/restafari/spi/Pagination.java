package org.projectodd.restafari.spi;


/** Pagination information for retrieving a subset of a collection.
 * 
 * @author Bob McWhirter
 */
public interface Pagination {
    
    /** Get the offset.
     * 
     * <p>
     * Offsets are zero-indexed.
     * </p>
     * 
     * @return The offset.
     */
    int getOffset();
    
    /** Get the number of items to return.
     * 
     * <p>
     * A limit of less-than-zero indicates no limit.
     * </p>
     * 
     * @return The limit.
     */
    int getLimit();

}
