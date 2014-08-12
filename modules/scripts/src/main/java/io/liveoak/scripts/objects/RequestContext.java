package io.liveoak.scripts.objects;


import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface RequestContext {

    SecurityContext getSecurityContext();

    Map<String, Object> getAttributes();

    Map<String, Object> getParameters();

}
