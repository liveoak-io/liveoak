/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.auth;

/**
 * TODO: Probably remove and init everything from JSON or something...
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzConstants {

    // Attribute where requestContext to authorize is saved
    public static final String ATTR_REQUEST_CONTEXT = "ATTR_REQUEST_CONTEXT";
    // Attribute where boolean decision of authzService response will be saved
    public static final String ATTR_AUTHZ_RESULT = "ATTR_AUTHZ_RESULT";
    // Attribute where authzDecision of policy response will be saved
    public static final String ATTR_AUTHZ_POLICY_RESULT = "ATTR_AUTHZ_POLICY_RESULT";

    public static final String AUTHZ_CHECK_RESOURCE_ID = "authzCheck";



    // Default applicationId. Metadata and default policy are actually registered for application with this ID
    public static final String DEFAULT_APP_ID = "DEFAULT_APP_ID";

    // Name of realm and application and publicKey, which will be registered by default under DEFAULT_APP_ID
    public static final String DEFAULT_REALM_NAME = "realmName1";
    public static final String DEFAULT_APPLICATION_NAME = "appName1";

}
