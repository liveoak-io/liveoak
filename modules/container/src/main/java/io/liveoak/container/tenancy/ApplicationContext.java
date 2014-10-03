package io.liveoak.container.tenancy;

import io.liveoak.common.MediaTypeMountPointResource;

/**
 * @author Bob McWhirter
 */
public class ApplicationContext extends MediaTypeMountPointResource {

    public ApplicationContext(InternalApplication application) {
        super(application.id());
        this.application = application;
    }

    public InternalApplication application() {
        return this.application;
    }

    public String toString() {
        return "[ApplicationContext: " + id() + "]";
    }

    private InternalApplication application;

}
