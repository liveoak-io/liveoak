package io.liveoak.scripts.objects.impl;

import io.liveoak.scripts.objects.Pagination;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakPagination implements Pagination {

    private final io.liveoak.spi.Pagination pagination;

    public LiveOakPagination(io.liveoak.spi.Pagination pagination) {
        this.pagination = pagination;
    }

    @Override
    public int getOffset() {
        return pagination.offset();
    }

    @Override
    public int getLimit() {
        return pagination.limit();
    }
}
