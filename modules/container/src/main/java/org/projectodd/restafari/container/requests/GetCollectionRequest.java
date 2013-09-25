package org.projectodd.restafari.container.requests;

import org.projectodd.restafari.spi.Pagination;

public class GetCollectionRequest extends BaseCollectionRequest implements Pagination {

    public GetCollectionRequest(String type, String collectionName) {
        super(type, collectionName);
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public int getOffset() {
        return this.offset;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public int getLimit() {
        return this.limit;
        
    }

    private int offset;
    private int limit = -1;
}
