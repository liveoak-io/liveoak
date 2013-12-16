package io.liveoak.security.policy.acl;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyConfigRule {

    // requestType for this request?
    private String requestType;

    // resource, from which we read the ACL state. By default it's the checked resource itself. It should probably support
    private String targetResourceURI;

    // attribute on the resource, which will contain name of the user. Check will pass if name of this attribute is same like name of current user
    // NOTE: value of attribute is supposed to be either String or Iterable
    private String allowedUserAttribute;

    // attribute on the resource, which will contain name of the role. Check will pass if name of this role is same like any roles of current user
    // NOTE: value of attribute is supposed to be either String or Iterable
    private String allowedRolesAttribute;

    // TODO: Add more things (maybe resourceType, maybe URIs as well)

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getTargetResourceURI() {
        return targetResourceURI;
    }

    public void setTargetResourceURI(String targetResourceURI) {
        this.targetResourceURI = targetResourceURI;
    }

    public String getAllowedUserAttribute() {
        return allowedUserAttribute;
    }

    public void setAllowedUserAttribute(String allowedUserAttribute) {
        this.allowedUserAttribute = allowedUserAttribute;
    }

    public String getAllowedRolesAttribute() {
        return allowedRolesAttribute;
    }

    public void setAllowedRolesAttribute(String allowedRolesAttribute) {
        this.allowedRolesAttribute = allowedRolesAttribute;
    }

    @Override
    public String toString() {
        return new StringBuilder("AclPolicyConfigRule [ ")
                .append(", requestType=").append(requestType)
                .append(", targetResourceURI=").append(targetResourceURI)
                .append(", allowedUserAttribute=").append(allowedUserAttribute)
                .append(", allowedRolesAttribute=").append(allowedRolesAttribute)
                .append(" ]").toString();
    }
}
