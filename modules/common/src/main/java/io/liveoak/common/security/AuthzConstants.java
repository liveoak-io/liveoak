/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.security;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzConstants {

    // Attribute where requestContext to authorize is saved
    public static final String ATTR_REQUEST_CONTEXT = "ATTR_REQUEST_CONTEXT";
    // Attribute where resourceState of request will be saved TODO: Remove after LIVEOAK-59 is done
    public static final String ATTR_REQUEST_RESOURCE_STATE = "ATTR_REQUEST_RESOURCE_STATE";
    // Attribute where boolean decision of authzService response will be saved
    public static final String ATTR_AUTHZ_RESULT = "ATTR_AUTHZ_RESULT";
    // Attribute where authzDecision of policy response will be saved
    public static final String ATTR_AUTHZ_POLICY_RESULT = "ATTR_AUTHZ_POLICY_RESULT";

    public static final String AUTHZ_CHECK_RESOURCE_ID = "authzCheck";

}
