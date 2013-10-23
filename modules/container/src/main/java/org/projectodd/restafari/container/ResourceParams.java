package org.projectodd.restafari.container;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class ResourceParams {

    public static final ResourceParams NONE = new ResourceParams();


    public static ResourceParams instance(Map<String, List<String>> params) {
        if (params == null || params.size() == 0) {
            return NONE;
        }
        return new ResourceParams(params);
    }

    private final Map<String, List<String>> params;

    private ResourceParams() {
        params = Collections.emptyMap();
    }

    private ResourceParams(Map<String, List<String>> params) {
        this.params = params;
    }

    public Collection<String> names() {
        return Collections.unmodifiableCollection(params.keySet());
    }

    public boolean contains(String name) {
        return value(name) != null;
    }

    public String value(String name) {
        List<String> values = params.get(name);
        if (values != null && values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    public List<String> values(String name) {
        return Collections.unmodifiableList(params.get(name));
    }

    public int getIntValue(String name, int def) {
        String val = value(name);
        if (val == null) {
            return def;
        }
        return Integer.parseInt(val);
    }
}
