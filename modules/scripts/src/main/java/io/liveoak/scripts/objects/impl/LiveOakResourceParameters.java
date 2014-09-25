package io.liveoak.scripts.objects.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.liveoak.common.DefaultReturnFields;
import io.liveoak.scripts.objects.Util;
import io.liveoak.scripts.objects.scripting.ScriptingRequestContext;
import io.liveoak.scripts.objects.scripting.ScriptingResourceParams;
import io.liveoak.spi.Sorting;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakResourceParameters extends LiveOakMap<String, Object> {

    ScriptingResourceParams resourceParams;
    ScriptingRequestContext context;

    public LiveOakResourceParameters(ScriptingRequestContext context) {
        this.context = context;
        this.resourceParams = (ScriptingResourceParams)context.resourceParams();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> entrySet = new HashSet<>();

        List<String> definedFields = Arrays.asList("offset", "limit", "sort", "fields");

        Entry offset = new LiveOakMapEntry<>("offset", context.pagination().offset());
        entrySet.add(offset);
        Entry limit = new LiveOakMapEntry<>("limit", context.pagination().limit());
        entrySet.add(limit);
        Entry fields = new LiveOakMapEntry<>("fields", Util.generateReturnFieldsString(context.returnFields()));
        entrySet.add(fields);

        if (context.sorting() != null && context.sorting().iterator() != null && context.sorting().iterator().hasNext()) {
            Entry sort = new LiveOakMapEntry<>("sort", Util.generateSortString(context.sorting()));
            entrySet.add(sort);
        }

        for (String paramName : resourceParams.names()) {
            if (!definedFields.contains(paramName)) {
                Entry entry = new LiveOakMapEntry<>(paramName, resourceParams.value(paramName));
                entrySet.add(entry);
            }
        }
        return entrySet;
    }

    @Override
    public String put(String key, Object value) {
        if (key.equals("offset")) {
            Integer offset = Util.getIntValue(value, context.pagination().offset());
            context.getScriptingPagination().offset(offset);
            resourceParams.setValue(key, offset.toString());
        } else if (key.equals("limit")) {
            Integer limit = Util.getIntValue(value, context.pagination().limit());
            context.getScriptingPagination().limit(limit);
            resourceParams.setValue(key, limit.toString());
        } else if (key.equals("sort") && value != null) {
            String sort = value.toString();
            context.sorting(new Sorting(sort));
            resourceParams.setValue(key, sort);
        } else if (key.equals("fields") && value != null) {
            String fields = value.toString();
            context.returnFields(new DefaultReturnFields(fields));
            resourceParams.setValue(key, fields);
        } else {
            resourceParams.setValue(key, String.valueOf(value));
        }
        return resourceParams.value(key);
    }

    @Override
    public String get(Object key) {
        return resourceParams.value((String) key);
    }

    @Override
    public String remove(Object key) {
        String current = resourceParams.value((String) key);
        resourceParams.remove((String)key);
        return current;
    }

    public String value(String key) {
        return resourceParams.value(key);
    }

    public List<String> values(String key) {
        return resourceParams.values(key);
    }

    public void setValue(String key, String value) {
        resourceParams.setValue(key, value );
    }

    public void setValues(String key, List<String> values) {
        resourceParams.setValue(key, values);
    }
}
