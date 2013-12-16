/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.security;

/**
 * Result of authorization check
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum AuthzDecision {
    ACCEPT, REJECT, IGNORE;


    /**
     * Merge this decision with another decision. Priorities are: REJECT, ACCEPT, IGNORE (REJECT always wins)
     *
     * @param newDecision decision to merge with this decision
     * @return result of merge
     */
    public AuthzDecision mergeDecision(AuthzDecision newDecision) {
        if (newDecision == AuthzDecision.REJECT || this == AuthzDecision.REJECT) {
            return AuthzDecision.REJECT;
        } else if (newDecision == AuthzDecision.ACCEPT || this == AuthzDecision.ACCEPT) {
            return AuthzDecision.ACCEPT;
        } else {
            return AuthzDecision.IGNORE;
        }
    }
}
