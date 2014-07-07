package io.liveoak.pgsql.meta;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class Catalog {

    private Map<TableRef, Table> tables;

    public Catalog(Map<TableRef, Table> tables) {
        this.tables = Collections.unmodifiableMap(tables);
    }

    public Table table(TableRef tableRef) {
        if (tableRef.schema() != null) {
            return tables.get(tableRef);
        } else {
            for (Table t: tables.values()) {
                if (t.name().equals(tableRef.name())) {
                    return t;
                }
            }
        }
        return null;
    }

    public List<String> tableIds() {
        LinkedHashMap<String, List<TableRef>> seenNames = new LinkedHashMap<>();
        for (TableRef ref: tables.keySet()) {
            List<TableRef> fullNames = seenNames.get(ref.name());
            if (fullNames == null) {
                fullNames = new LinkedList<>();
                seenNames.put(ref.name(), fullNames);
            }
            fullNames.add(ref);
        }

        // now construct the response
        List<String> ret = new LinkedList<>();
        for (Map.Entry<String, List<TableRef>> e: seenNames.entrySet()) {
            if (e.getValue().size() > 1) {
                for (TableRef ref: e.getValue()) {
                    ret.add(ref.asUnquotedIdentifier());
                }
            } else {
                ret.add(e.getKey());
            }
        }

        return ret;
    }
}
