package io.liveoak.scripts.objects.scripting;

import io.liveoak.spi.Application;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.SecurityContext;
import io.liveoak.spi.Sorting;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptingRequestContext implements RequestContext {

    private RequestContext original;
    private ScriptingPagination pagination;
    private ReturnFields returnFields;
    private ResourceParams params;
    private Sorting sorting;

    public ScriptingRequestContext(RequestContext original) {
        this.original = original;
        pagination = new ScriptingPagination(original.pagination());
        returnFields = original.returnFields();
        params = new ScriptingResourceParams(original.resourceParams());
        sorting = original.sorting();
    }

    @Override
    public Application application() {
        return original.application();
    }

    @Override
    public SecurityContext securityContext() {
        return original.securityContext();
    }

    @Override
    public Pagination pagination() {
        return pagination;
    }

    public ScriptingPagination getScriptingPagination() {
        return this.pagination;
    }

    public void pagination(ScriptingPagination pagination) {
        this.pagination = pagination;
    }

    @Override
    public ResourcePath resourcePath() {
        return original.resourcePath();
    }

    @Override
    public ResourceParams resourceParams() {
        return params;
    }

    @Override
    public RequestAttributes requestAttributes() {
        return original.requestAttributes();
    }

    @Override
    public RequestType requestType() {
        return original.requestType();
    }

    @Override
    public ReturnFields returnFields() {
        return returnFields;
    }

    public void returnFields(ReturnFields returnFields) {
        this.returnFields = returnFields;
    }

    @Override
    public Sorting sorting() {
        return sorting;
    }

    public void sorting(Sorting sorting) {
        this.sorting = sorting;
    }

    @Override
    public void dispose() {
       original.dispose();
    }

    @Override
    public void onDispose(Runnable runnable) {
        original.onDispose(runnable);
    }
}
