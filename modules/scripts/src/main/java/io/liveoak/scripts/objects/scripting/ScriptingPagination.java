package io.liveoak.scripts.objects.scripting;

import io.liveoak.spi.Pagination;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptingPagination implements Pagination {

    int offset;
    int limit;

    public ScriptingPagination(Pagination original) {
        offset = original.offset();
        limit = original.limit();
    }

    public ScriptingPagination(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public int offset() {
        return offset;
    }

    public void offset(int offset) {
        this.offset = offset;
    }

    @Override
    public int limit() {
        return limit;
    }

    public void limit(int limit) {
        this.limit = limit;
    }
}
