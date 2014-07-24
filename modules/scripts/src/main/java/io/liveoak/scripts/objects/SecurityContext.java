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
}
