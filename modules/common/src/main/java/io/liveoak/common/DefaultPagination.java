package io.liveoak.common;

import io.liveoak.spi.Pagination;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class DefaultPagination implements Pagination {

    private int offset;
    private int limit;

    public DefaultPagination(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public int limit() {
        return limit;
    }
}
