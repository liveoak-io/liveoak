/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri.complex;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates info about processing of URI string with one single rule of URIPolicyRule
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIMatcher {

    // true if this matcher has been already processed
    private boolean processed;

    // true if URI matches the pattern for this policy
    private boolean matched;

    // matcher groups
    private List<String> groups = new ArrayList<String>();

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public void addGroup(String group) {
        groups.add(group);
    }

    /**
     * group(0) is whole URI.
     * group(X) for X greater than 0 are matcher groups from particular regex.
     *
     * @param groupIndex
     * @return group from regex or null if groupIndex is bigger than number of groups (Method never throws ArrayIndexOutOfBoundsException)
     */
    public String group(int groupIndex) {
        if (groups.size() > groupIndex) {
            return groups.get(groupIndex);
        } else {
            return null;
        }
    }

    public String toString() {
        return new StringBuilder("URIMatcher [processed=")
                .append(processed)
                .append(", matched=")
                .append(matched)
                .append(", groups=")
                .append(groups)
                .append("]").toString();
    }
}
