package io.liveoak.container.tenancy;

import io.liveoak.container.tenancy.service.OrganizationService;
import io.liveoak.container.zero.OrganizationResource;
import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 */
public class InternalOrganizationRegistry implements OrganizationRegistry {

    private final ServiceTarget target;

    public InternalOrganizationRegistry(ServiceTarget target) {
        this.target = target;
    }

    public InternalOrganization createOrganization(String id, String name) throws InterruptedException {
        OrganizationService org = new OrganizationService( id, name );

        ServiceController<InternalOrganization> controller = this.target.addService(LiveOak.organization(id), org )
                .install();

        this.organizations.put(id, controller);
        return controller.awaitValue();
    }

    public void destroyOrganization(InternalOrganization org) {
        ServiceController<InternalOrganization> controller = this.organizations.remove(org.id());
        controller.setMode(ServiceController.Mode.REMOVE);
    }

    public Collection<InternalOrganization> organizations() {
        return Collections.unmodifiableCollection(this.organizations.values().stream().map( e->e.getValue() ).collect(Collectors.toList() ));
    }

    public InternalOrganization organization(String id) {
        ServiceController<InternalOrganization> controller = this.organizations.get(id);
        if ( controller == null ) {
            return null;
        }

        return controller.getValue();
    }

    private Map<String, ServiceController<InternalOrganization>> organizations = new HashMap<>();

}
