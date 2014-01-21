package io.liveoak.security.policy.acl;

import java.util.Set;

import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyCheckResource implements Resource {

    private static final Logger log = Logger.getLogger(AclPolicyCheckResource.class);

    private final String id;
    private final Resource parent;
    private final AclPolicyConfig policy;
    private final Client client;

    public AclPolicyCheckResource(AclPolicyRootResource parent, String id, AclPolicyConfig policy, Client client) {
        this.id = id;
        this.parent = parent;
        this.policy = policy;
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
        try {
            if (this.policy != null) {
                RequestContext reqCtxToAuthorize = ctx.requestAttributes() != null ? ctx.requestAttributes().getAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, RequestContext.class) : null;

                if (reqCtxToAuthorize == null) {
                    if (log.isTraceEnabled()) {
                        log.trace("Request is null. Rejecting");
                    }
                    sendDecision(AuthzDecision.REJECT, sink);
                } else {
                    checkAuthorized(reqCtxToAuthorize, this.policy, sink);
                }
            } else {
                log.warn("Configuration not available. Ignoring");
                sendDecision(AuthzDecision.IGNORE, sink);
            }
        } catch (Throwable t) {
            log.error("Error during authz check", t);
            sendDecision(AuthzDecision.REJECT, sink);
        }
    }

    protected void checkAuthorized(RequestContext reqCtxToAuthorize, AclPolicyConfig aclPolicyConfig, PropertySink sink) throws Exception {
        AclPolicyConfigRule configRule = getBestRule(reqCtxToAuthorize, aclPolicyConfig);
        if (configRule == null) {
            if (log.isTraceEnabled()) {
                log.trace("No rule found for request " + reqCtxToAuthorize + ". Ignoring");
            }
            sendDecision(AuthzDecision.IGNORE, sink);
        } else {
            String targetResourcePath = configRule.getTargetResourceURI()!=null ? configRule.getTargetResourceURI() : reqCtxToAuthorize.resourcePath().toString();

            // Async call to retrieve target resource
            client.read(reqCtxToAuthorize, targetResourcePath, (resourceResponse) -> {
                authzCallback(reqCtxToAuthorize, resourceResponse, configRule, sink);
            });
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

    protected void authzCallback(RequestContext reqCtxToAuthorize, ClientResourceResponse response, AclPolicyConfigRule configRule, PropertySink sink) {
        ResourceState resourceState = response.state();
        AuthzDecision decision = AuthzDecision.IGNORE;

        if (response.responseType() != ClientResourceResponse.ResponseType.OK || resourceState == null) {
            if (log.isTraceEnabled()) {
                log.trace("Incorrect responseType or resourceState is null. responseType: " + response.responseType() + ", resourceState: " + resourceState);
            }
        } else {

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
        }

        // If rule exists, but neither allowedUser or allowedRoles passed, we will ignore (TODO: configurable? Or have some clever voter's based mechanism?)
        /*if (decision == AuthzDecision.IGNORE) {
            decision = AuthzDecision.REJECT;
        } */
        try {
            sendDecision(decision, sink);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected AuthzDecision verifyProperty(Object propertyValue, String expectedValue) {
        if (propertyValue == null) {
            return AuthzDecision.IGNORE;
        } else if (propertyValue instanceof String) {
            String propValue = (String)propertyValue;
            if (propValue.equals(expectedValue)) {
                return AuthzDecision.ACCEPT;
            }
        } else if (propertyValue instanceof Iterable) {
            Iterable<String> iterableValue = (Iterable<String>)propertyValue;
            for (String propValue : iterableValue) {
                if (propValue.equals(expectedValue)) {
                    return AuthzDecision.ACCEPT;
                }
            }
        } else {
            log.warn("Unsupported type of property: " + propertyValue);
        }

        return AuthzDecision.IGNORE;
    }

    protected void sendDecision(AuthzDecision decision, PropertySink sink) throws Exception {
        sink.accept(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT, decision.toString());
        sink.close();
    }
}
