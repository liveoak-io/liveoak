/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.spi;

/**
 * Result of authorization check
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum AuthorizationDecision {
    ACCEPT, REJECT, IGNORE;


    /**
     * Merge this decision with another decision. Priorities are: REJECT, ACCEPT, IGNORE (REJECT always wins)
     *
     * @param newDecision decision to merge with this decision
     * @return result of merge
     */
    public AuthorizationDecision mergeDecision( AuthorizationDecision newDecision ) {
        if ( newDecision == AuthorizationDecision.REJECT || this == AuthorizationDecision.REJECT ) {
            return AuthorizationDecision.REJECT;
        } else if ( newDecision == AuthorizationDecision.ACCEPT || this == AuthorizationDecision.ACCEPT ) {
            return AuthorizationDecision.ACCEPT;
        } else {
            return AuthorizationDecision.IGNORE;
        }
    }
}
