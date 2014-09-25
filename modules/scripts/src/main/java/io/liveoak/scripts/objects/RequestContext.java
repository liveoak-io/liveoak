package io.liveoak.scripts.objects;


import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface RequestContext {

    SecurityContext getSecurityContext();

    Map<String, Object> getAttributes();

    Map<String, Object> getParameters();

    default void setParameters(Object parameters) throws Exception {
        throw Util.notEditable("parameters");
    }

    default void setAttributes(Object attributes) throws Exception {
        throw Util.notEditable("attributes");
    }

    default void setSecurityContext(Object securityContext) throws Exception {
        throw Util.notEditable("securityContext");
    }
}
