package io.liveoak.container.zero;

import io.liveoak.container.tenancy.InternalOrganization;
import io.liveoak.spi.Organization;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Bob McWhirter
 */
public class OrganizationResource implements RootResource, SynchronousResource {

    public OrganizationResource(InternalOrganization org) {
        this.org = org;
        this.apps = new ApplicationsResource( this );
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.org.id();
    }

    @Override
    public Collection<? extends Resource> members() {
        return Collections.singleton( this.apps );
    }

    @Override
    public Resource member(String id) {
        if ( id.equals( apps.id() ) ) {
            return apps;
        }

        return null;
    }

    public InternalOrganization organization() {
        return this.org;
    }

    public ApplicationsResource applicationsResource() {
        return this.apps;
    }

    private Resource parent;
    private ApplicationsResource apps;
    private InternalOrganization org;

}
