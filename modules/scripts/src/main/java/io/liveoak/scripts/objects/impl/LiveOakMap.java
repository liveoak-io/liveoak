package io.liveoak.scripts.objects.impl;

import java.util.AbstractMap;
import java.util.Iterator;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public abstract class LiveOakMap<K, V> extends AbstractMap<K, V> {

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");

        Iterator iterator = entrySet().iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next());
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }

        builder.append("}");
        return builder.toString();
    }
}
