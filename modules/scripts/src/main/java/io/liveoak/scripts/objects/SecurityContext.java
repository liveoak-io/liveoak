package io.liveoak.scripts.objects;

import java.util.List;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface SecurityContext {

    Boolean getAuthenticated();

    String getRealm();

    String getSubject();

    Long getLastVerified();

    List<String> getRoles();

    String getToken();

    Boolean hasRole(String role);

    default void setAuthenticated(Object object) {
        Util.notEditable("authenticated");
    }

    default void setRealm(Object object) {
        Util.notEditable("realm");
    }

    default void setLastVerified(Object object) {
        Util.notEditable("lastVerified");
    }

    default void setRoles(Object object) {
        Util.notEditable("roles");
    }

    default void setToken(Object object) {
        Util.notEditable("token");
    }
}
