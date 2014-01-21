package io.liveoak.container.tenancy;

import io.liveoak.spi.Organization;

import java.util.Collection;

/**
 * @author Bob McWhirter
 */
public interface OrganizationRegistry {

    Collection<? extends Organization> organizations();
    Organization organization(String id);


}
