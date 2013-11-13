package io.liveoak.security.policy.uri.complex;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates info about processing of URI string with one single rule of URIPolicyEntry
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
     * group(X) for X>=0 are matcher groups from particular regex.
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
