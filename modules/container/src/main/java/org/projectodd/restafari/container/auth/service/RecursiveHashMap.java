package org.projectodd.restafari.container.auth.service;

import java.util.*;

/**
 * Utility to save authorization rules and then find best {@link RolePolicy} for given request
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RecursiveHashMap extends HashMap<String, Object> {

    private RecursiveHashMap parent;

    public RecursiveHashMap(RecursiveHashMap parent) {
        this.parent = parent;
    }

    public void recursivePut(Deque<String> keys, RolePolicy value) {
        String topKey = keys.remove();
        if (keys.isEmpty()) {
            this.put(topKey, value);
        } else {
            RecursiveHashMap childMap = (RecursiveHashMap)this.get(topKey);
            if (childMap == null) {
                childMap = new RecursiveHashMap(this);
                this.put(topKey, childMap);
            }
            childMap.recursivePut(keys, value);
        }
    }

    public RolePolicy recursiveGet(Deque<String> keys) {
        return recursiveGet(keys, new HashSet<>());
    }

    protected RolePolicy recursiveGet(Deque<String> keys, Collection<RecursiveHashMap> processedAncestors) {
        String topKey = keys.isEmpty() ? RoleBasedAuthorizationService.WILDCARD : keys.peek();

        // Try to find policy exactly for the key
        if (this.containsKey(topKey)) {
            return safeGet(topKey, keys, processedAncestors);
        }

        // Try to find policy for * if policy for the key not available
        if (topKey != RoleBasedAuthorizationService.WILDCARD) {
            if (this.containsKey(RoleBasedAuthorizationService.WILDCARD)) {
                return safeGet(RoleBasedAuthorizationService.WILDCARD, keys, processedAncestors);
            }
        }

        // No policy for our key or for *. We need to fallback and check parent or other ancestors
        int ancestorDepth = 0;
        RecursiveHashMap bestAncestor = parent;
        while (processedAncestors.contains(bestAncestor)) {
            bestAncestor = bestAncestor.parent;
            ancestorDepth++;
        }

        processedAncestors.add(bestAncestor);

        Deque<String> newKeys = new LinkedList<>();
        int size = keys.size() + ancestorDepth;
        for (int i=0 ; i<size ; i++) {
            // Add all path segments with wildcard
            newKeys.add(RoleBasedAuthorizationService.WILDCARD);
        }
        // Add action to the last position without wildcard
        newKeys.add(keys.getLast());

        return bestAncestor.recursiveGet(newKeys, processedAncestors);
    }

    // It's safe in the sense that this.get(topKey) is not null (method should never be called if this condition is not true)
    private RolePolicy safeGet(String topKey, Deque<String> keys, Collection<RecursiveHashMap> processedAncestors) {
        Object o = this.get(topKey);
        if (o instanceof RolePolicy) {
            return (RolePolicy)o;
        } else {
            RecursiveHashMap childMap = (RecursiveHashMap)o;
            // Now remove key
            keys.poll();
            return childMap.recursiveGet(keys, processedAncestors);
        }
    }
}
