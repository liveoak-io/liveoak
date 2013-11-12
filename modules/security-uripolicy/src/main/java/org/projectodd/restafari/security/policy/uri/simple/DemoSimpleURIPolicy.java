package org.projectodd.restafari.security.policy.uri.simple;

import org.projectodd.restafari.security.policy.uri.RolesContainer;
import org.projectodd.restafari.spi.RequestType;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DemoSimpleURIPolicy extends SimpleURIPolicy {

    public DemoSimpleURIPolicy() {
        // Everything is allowed, unless specially configured
        addRolePolicy("*", "*", "*", "*", ALLOW_ALL_ROLES_CONTAINER);

        // Collection protected1 is available for READ for role "users"
        addRolePolicy("authTest", "protected1", "*", RequestType.READ.name(),
                new RolesContainer().addAllowedApplicationRole("user"));

        // Collection protected1 is available for other actions than READ for role "admins" (READ specified by previous rule)
        addRolePolicy("authTest", "protected1", "*", "*",
                new RolesContainer().addAllowedApplicationRole("admin"));

        // Resource "12345" in collection "protected1" is available for "users" and "powerUsers" for all requestTypes (readMember+write)
        addRolePolicy("authTest", "protected1", "12345", "*",
                new RolesContainer().addAllowedApplicationRole("user").addAllowedApplicationRole("powerUser"));

        // Collection protected2 is available for CREATE for realm role "users"
        addRolePolicy("authTest", "protected2", "*",RequestType.CREATE.name(),
                new RolesContainer().addAllowedRealmRole("user"));
    }
}
