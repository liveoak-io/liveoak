package io.liveoak.scripts.objects;

import java.util.List;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface Sorting {

    List<Spec> getSorting();

    interface Spec {
        String getName();

        void setName(String name);

        Boolean getAscending();

        void setAscending(Boolean ascending);
    }

}
