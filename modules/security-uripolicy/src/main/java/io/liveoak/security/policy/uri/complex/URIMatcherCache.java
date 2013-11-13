package io.liveoak.security.policy.uri.complex;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds info about all processed URIMatcher by all URIPolicyEntry rules
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIMatcherCache {

    // Key is name of drools rule. Value is URIMatcher coming from processing of this rule
    private Map<String, URIMatcher> cache = new ConcurrentHashMap<String, URIMatcher>();

    /**
     * @return URIMatcher for given rule or create new URIMatcher if it not exists yet. Method never returns null
     */
    public URIMatcher getURIMatcher(String key) {
        URIMatcher mi = cache.get(key);
        if (mi == null) {
            mi = new URIMatcher();
            cache.put(key, mi);
        }
        return mi;
    }
}
