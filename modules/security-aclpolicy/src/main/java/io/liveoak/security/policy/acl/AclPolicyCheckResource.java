package io.liveoak.security.policy.acl;

import java.util.Set;

import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyCheckResource implements Resource, BlockingResource {

    private static final Logger log = Logger.getLogger(AclPolicyCheckResource.class);

    private final String id;
    private final AclPolicyRootResource parent;
    private final Client client;

    public AclPolicyCheckResource(String id, AclPolicyRootResource parent, Client client) {
        this.id = id;
        this.parent = parent;
        this.client = client;
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
            AclPolicyConfig aclPolicyConfig = parent.getPolicyConfig();
            if (aclPolicyConfig != null) {
                RequestContext reqCtxToAuthorize = ctx.requestAttributes() != null ? ctx.requestAttributes().getAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, RequestContext.class) : null;

                if (reqCtxToAuthorize == null) {
                    if (log.isTraceEnabled()) {
                        log.trace("Request is null. Rejecting");
                    }
                    decision = AuthzDecision.REJECT;
                } else {
                    decision = isAuthorized(reqCtxToAuthorize, aclPolicyConfig);
                }
            } else {
                log.warn("Configuration not available. Ignoring");
            }
        } catch (Throwable t) {
            log.error("Error during authz check", t);
            decision = AuthzDecision.REJECT;
        }

        if (decision == null) {
            decision = AuthzDecision.IGNORE;
        }

        sink.accept(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT, decision.toString());
        sink.close();
    }

    protected AuthzDecision isAuthorized(RequestContext reqCtxToAuthorize, AclPolicyConfig aclPolicyConfig) {
        AclPolicyConfigRule configRule = getBestRule(reqCtxToAuthorize, aclPolicyConfig);
        if (configRule == null) {
            if (log.isTraceEnabled()) {
                log.trace("No rule found for request " + reqCtxToAuthorize + ". Ignoring");
            }
            return AuthzDecision.IGNORE;
        } else {
            String targetResourcePath = configRule.getTargetResourceURI()!=null ? configRule.getTargetResourceURI() : reqCtxToAuthorize.resourcePath().toString();

            try {
                AuthzDecision decision = AuthzDecision.IGNORE;

                // TODO: use async if possible and makes sense. Don't forget to remove BlockingResource in that case!
                ResourceState resourceState = client.read(reqCtxToAuthorize, targetResourcePath);

                // allowedUser check
                if (configRule.getAllowedUserAttribute() != null) {
                    Object usernameProperty = resourceState.getProperty(configRule.getAllowedUserAttribute());
                    AuthzDecision usernameDecision = verifyProperty(usernameProperty, reqCtxToAuthorize.securityContext().getSubject());
                    decision = decision.mergeDecision(usernameDecision);
                }

                // allowedRoles check
                if (configRule.getAllowedRolesAttribute() != null) {
                    Set<String> userRoles = reqCtxToAuthorize.securityContext().getRoles();
                    Object roleProperty = resourceState.getProperty(configRule.getAllowedRolesAttribute());
                    if (userRoles != null) {
                        for (String userRole : userRoles) {
                            AuthzDecision currentRoleDecision = verifyProperty(roleProperty, userRole);
                            decision = decision.mergeDecision(currentRoleDecision);
                        }
                    }
                }

                // If rule exists, but neither allowedUser or allowedRoles passed, we will reject
                if (decision == AuthzDecision.IGNORE) {
                    decision = AuthzDecision.REJECT;
                }

                return decision;
            } catch (Exception e) {
                log.warn("Error occured during aclPolicy authz check. Details: " + e.getMessage());
                return AuthzDecision.IGNORE;
            }
        }
    }

    // Obtain just one rule for now
    protected AclPolicyConfigRule getBestRule(RequestContext reqCtxToAuthorize, AclPolicyConfig aclPolicyConfig) {
        AclPolicyConfigRule wildcardRule = null;

        for (AclPolicyConfigRule configRule : aclPolicyConfig.getAclRules()) {
            if (configRule.getRequestType().equals(reqCtxToAuthorize.requestType().toString())) {
                return configRule;
            } else if (configRule.getRequestType().equals("*")) {
                wildcardRule = configRule;
            }
        }

        // Return wildcard rule only if no rule of exact type has been found
        return wildcardRule;
    }

    protected AuthzDecision verifyProperty(Object property, String expectedValue) {
        if (property == null) {
            return AuthzDecision.REJECT;
        } else if (property instanceof String) {
            String propValue = (String)property;
            if (propValue.equals(expectedValue)) {
                return AuthzDecision.ACCEPT;
            }
        } else if (property instanceof Iterable) {
            Iterable<String> iterableValue = (Iterable<String>)property;
            for (String propValue : iterableValue) {
                if (propValue.equals(expectedValue)) {
                    return AuthzDecision.ACCEPT;
                }
            }
        } else {
            log.warn("Unsupported type of property: " + property);
        }

        return AuthzDecision.IGNORE;
    }

}
