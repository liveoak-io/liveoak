package io.liveoak.scripts.objects;


import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface RequestContext {

    Application getApplication();

    SecurityContext getSecurityContext();

    Pagination getPagination();

    Map<String, Object> getRequestAttributes();

    List<Sorting.Spec> getSorting();

    //TODO: figure out how to handle the ReturnFields.
}
