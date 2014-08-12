package io.liveoak.scripts.objects.scripting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.liveoak.spi.ResourceParams;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptingResourceParams implements ResourceParams{

    Map<String, List<String>> params;

    public ScriptingResourceParams(ResourceParams resourceParams) {
        params = new HashMap<>();
        for (String name: resourceParams.names()) {
            params.put(name, resourceParams.values(name));
        }
    }

    public ScriptingResourceParams() {
        this.params = new HashMap<>();
    }

    @Override
    public Collection<String> names() {
        return params.keySet();
    }

    @Override
    public boolean contains(String name) {
        return params.containsKey(name);
    }

    @Override
    public String value(String name) {
        List<String> values = params.get(name);
        if (values != null && values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    @Override
    public List<String> values(String name) {
        return params.get(name);
    }

    public void setValue(String name, String value) {
        List<String> list = new ArrayList<>();
        list.add(value);
        setValue(name, list);
    }

    public void setValue(String name, List<String> value) {
        params.put(name, value);
    }

    public void remove(String name) {
        params.remove(name);
    }

}
