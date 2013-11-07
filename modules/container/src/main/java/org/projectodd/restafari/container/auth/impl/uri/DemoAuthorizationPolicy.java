package org.projectodd.restafari.container.auth.impl.uri;

import org.projectodd.restafari.container.ResourceRequest;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DemoAuthorizationPolicy extends URIAuthorizationPolicy {

    public DemoAuthorizationPolicy() {
        // Everything is allowed, unless specially configured
        addRolePolicy("*", "*", "*", "*", ALLOW_ALL_ROLE_POLICY);

        // Collection protected1 is available for READ for role "users"
        addRolePolicy("authTest", "protected1", "*", ResourceRequest.RequestType.READ.name(),
                new RolePolicy.RolePolicyBuilder().addAllowedApplicationRole("users").build());

        // Collection protected1 is available for other actions than READ for role "admins" (READ specified by previous rule)
        addRolePolicy("authTest", "protected1", "*", "*",
                new RolePolicy.RolePolicyBuilder().addAllowedApplicationRole("admins").build());

        // Resource "12345" in collection "protected1" is available for "users" and "powerUsers" for all requestTypes (readMember+write)
        addRolePolicy("authTest", "protected1", "12345", "*",
                new RolePolicy.RolePolicyBuilder().addAllowedApplicationRole("users").addAllowedApplicationRole("powerUsers").build());

        // Collection protected2 is available for CREATE for realm role "users"
        addRolePolicy("authTest", "protected2", "*", ResourceRequest.RequestType.CREATE.name(),
                new RolePolicy.RolePolicyBuilder().addAllowedRealmRole("users").build());
    }
}
