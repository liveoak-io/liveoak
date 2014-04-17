/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri.complex;

import io.liveoak.spi.RequestType;

/**
 * Single entry of {@link URIPolicy}
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyRule {

    private final int id;
    private final int priority;
    private final String uriPattern;
    private final String queryParamsCondition;
    private final String requestType;

    private final String allowedRoles;
    private final String deniedRoles;
    private final String allowedUsers;
    private final String deniedUsers;

    private URIPolicyRule(int id, int priority, String uriPattern, String queryParamsCondition, String requestType, String allowedRoles, String deniedRoles,
                          String allowedUsers, String deniedUsers) {
        this.id = id;
        this.priority = priority;
        this.uriPattern = uriPattern;
        this.queryParamsCondition = queryParamsCondition;
        this.requestType = requestType;

        this.allowedRoles = allowedRoles;
        this.allowedUsers = allowedUsers;
        this.deniedRoles = deniedRoles;
        this.deniedUsers = deniedUsers;
    }

    public static URIPolicyRule createRule(int id, int priority, String uriPattern, String queryParamsCondition, String requestType,
                                           String allowedRoles, String deniedRoles, String allowedUsers, String deniedUsers) {

        // From uriPattern from "user-friendly" form to "drools-friendly" form
        String formattedPattern = DroolsFormattingUtils.formatStringToDrools(uriPattern);

        // Add placeholder for queryParamsCondition if it's empty
        if (queryParamsCondition == null || queryParamsCondition.isEmpty()) {
            queryParamsCondition = "1 == 1";
        }

        // Multiply priority by 2 (we will have URIPolicy rules with 'even' salience and checking rules with 'odd' salience)
        priority = priority * 2;

        // RequestType must be wildcard or some of RequestType enums
        requestType = validateAndFormatRequestType(requestType);

        return new URIPolicyRule(id, priority, formattedPattern, queryParamsCondition, requestType, allowedRoles, deniedRoles,
                allowedUsers, deniedUsers);
    }

    public int getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public String getUriPattern() {
        return uriPattern;
    }

    public String getQueryParamsCondition() {
        return queryParamsCondition;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getAllowedRoles() {
        return allowedRoles;
    }

    public String getDeniedRoles() {
        return deniedRoles;
    }

    public String getAllowedUsers() {
        return allowedUsers;
    }

    public String getDeniedUsers() {
        return deniedUsers;
    }

    /**
     * requestType must be either "*" or set of requestTypes divided by comma (for example: "CREATE, READ, DELETE" or "DELETE")
     * Method throws IllegalArgumentException if requestType is not formatted
     *
     * @param requestType
     * @return formatted requestType without spaces and with wildcard expanded to all request types (like "CREATE,READ,UPDATE,DELETE")
     */
    private static String validateAndFormatRequestType(String requestType) {
        if (requestType == null) {
            throw new IllegalArgumentException("requestType can't be null");
        } else if (requestType.equals("*")) {
            requestType = "CREATE,READ,UPDATE,DELETE";
        }
        String[] spl = requestType.split(",");
        StringBuilder requestTypeBuilder = new StringBuilder();
        for (int i = 0; i < spl.length; i++) {
            String oneReqType = spl[i].trim();
            // Just to check if it throws IllegalArgumentException
            Enum.valueOf(RequestType.class, oneReqType);

            if (i != 0) {
                requestTypeBuilder.append(",");
            }
            requestTypeBuilder.append(oneReqType);
        }
        return requestTypeBuilder.toString();
    }
}
