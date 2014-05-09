package io.liveoak.container.tenancy.service;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.zero.ApplicationExtensionsResource;
import io.liveoak.container.tenancy.ApplicationResource;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class ApplicationResourceService implements Service<ApplicationResource> {

    public ApplicationResourceService(InternalApplication app) {
        this.app = app;
    }

    @Override
    public void start(StartContext context) throws StartException {

        this.resource = new ApplicationResource(this.app);

        ServiceTarget target = context.getChildTarget();
        ServiceName name = context.getController().getName();

        target.addService( name.append( "extensions" ), new ValueService<ApplicationExtensionsResource>( new ImmediateValue<>( this.resource.extensionsResource() ) ) )
                .install();
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public ApplicationResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    private final InternalApplication app;
    private ApplicationResource resource;

}
