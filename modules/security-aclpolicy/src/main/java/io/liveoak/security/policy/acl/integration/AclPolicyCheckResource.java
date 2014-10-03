package io.liveoak.security.policy.acl.integration;

import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.security.policy.acl.impl.AclPolicy;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyCheckResource implements Resource {

    private static final Logger log = Logger.getLogger(AclPolicyCheckResource.class);

    private final String id;
    private final AclPolicyRootResource parent;
    private final AclPolicy policy;

    public AclPolicyCheckResource(AclPolicyRootResource parent, String id, AclPolicy policy) {
        this.id = id;
        this.parent = parent;
        this.policy = policy;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        AuthzDecision decision = null;

        try {
            if (policy != null) {
                RequestContext reqCtxToAuthorize = ctx.requestAttributes() != null ? ctx.requestAttributes().getAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, RequestContext.class) : null;
                if (reqCtxToAuthorize == null) {
                    if (log.isTraceEnabled()) {
                        log.trace("Request is null. Rejecting");
                    }
                    decision = AuthzDecision.REJECT;
                } else {
                    decision = policy.isAuthorized(reqCtxToAuthorize);
                }
            }
        } catch (Throwable t) {
            log.error("Error during authz check", t);
            decision = AuthzDecision.REJECT;
        }

        if (decision == null) {
            decision = AuthzDecision.IGNORE;
        }

        sink.accept(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT, decision.toString());
        sink.complete();
    }
}
