/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri.simple;


import io.liveoak.security.policy.uri.RolesContainer;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Utility to save authorization rules and then find best RolesContainer for given request
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RecursiveHashMap extends HashMap<String, Object> {

    private RecursiveHashMap parent;

    public RecursiveHashMap(RecursiveHashMap parent) {
        this.parent = parent;
    }

    public void recursivePut(Deque<String> keys, RolesContainer value) {
        String topKey = keys.remove();
        if (keys.isEmpty()) {
            this.put(topKey, value);
        } else {
            RecursiveHashMap childMap = (RecursiveHashMap) this.get(topKey);
            if (childMap == null) {
                childMap = new RecursiveHashMap(this);
                this.put(topKey, childMap);
            }
            childMap.recursivePut(keys, value);
        }
    }

    public RolesContainer recursiveGet(Deque<String> keys) {
        return recursiveGet(keys, new HashSet<>());
    }

    protected RolesContainer recursiveGet(Deque<String> keys, Collection<RecursiveHashMap> processedAncestors) {
        String topKey = keys.isEmpty() ? SimpleURIPolicy.WILDCARD : keys.peek();

        // Try to find policy exactly for the key
        if (this.containsKey(topKey)) {
            return safeGet(topKey, keys, processedAncestors);
        }

        // Try to find policy for * if policy for the key not available
        if (topKey != SimpleURIPolicy.WILDCARD) {
            if (this.containsKey(SimpleURIPolicy.WILDCARD)) {
                return safeGet(SimpleURIPolicy.WILDCARD, keys, processedAncestors);
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
        for (int i = 0; i < size; i++) {
            // Add all path segments with wildcard
            newKeys.add(SimpleURIPolicy.WILDCARD);
        }
        // Add action to the last position without wildcard
        newKeys.add(keys.getLast());

        return bestAncestor.recursiveGet(newKeys, processedAncestors);
    }

    // It's safe in the sense that this.get(topKey) is not null (method should never be called if this condition is not true)
    private RolesContainer safeGet(String topKey, Deque<String> keys, Collection<RecursiveHashMap> processedAncestors) {
        Object o = this.get(topKey);
        if (o instanceof RolesContainer) {
            return (RolesContainer) o;
        } else {
            RecursiveHashMap childMap = (RecursiveHashMap) o;
            // Now remove key
            keys.poll();
            return childMap.recursiveGet(keys, processedAncestors);
        }
    }
}
