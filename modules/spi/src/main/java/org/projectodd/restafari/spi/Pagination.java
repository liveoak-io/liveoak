package org.projectodd.restafari.spi;


/** Pagination information for retrieving a subset of a collection.
 * 
 * @author Bob McWhirter
 */
public interface Pagination {

    static final int DEFAULT_LIMIT = 100;

    static final int MAX_LIMIT = 10000;

    static final Pagination NONE = new Pagination() {
        public int getOffset() {
            return 0;
        }
        public int getLimit() {
            return DEFAULT_LIMIT;
        }
    };

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
