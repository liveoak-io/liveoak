package io.liveoak.pgsql.meta;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class Catalog {

    private final Set<String> schemas;
    private final String defaultSchema;
    private final Map<TableRef, Table> tables;

    public Catalog(Set<String> schemas, String defaultSchema, Map<TableRef, Table> tables) {
        this.schemas = Collections.unmodifiableSet(schemas);
        this.defaultSchema = defaultSchema;

        Map<TableRef, Table> tablesWithIds = enhanceWithIdsAndReferredFKs(tables);
        this.tables = Collections.unmodifiableMap(tablesWithIds);

        // set catalog on tables
        for (Table t: this.tables.values()) {
            t.catalog(this);
        }
    }

    public Catalog(Catalog catalog, List<Table> tablesToAdd) {
        Map<TableRef, Table> newTables = new HashMap<>();
        for (Map.Entry<TableRef, Table> e: catalog.tables.entrySet()) {
            Table t = e.getValue();
            newTables.put(e.getKey(), t.copy());
        }
        for (Table t: tablesToAdd) {
            newTables.put(t.tableRef(), t.copy());
        }

        Map<TableRef, Table> tablesWithIds = enhanceWithIdsAndReferredFKs(newTables);
        this.schemas = catalog.schemas;
        this.defaultSchema = catalog.defaultSchema;
        this.tables = Collections.unmodifiableMap(tablesWithIds);

        // set catalog on tables
        for (Table t: this.tables.values()) {
            t.catalog(this);
        }
    }

    protected Map<TableRef, Table> enhanceWithIdsAndReferredFKs(Map<TableRef, Table> tables) {
        Map<TableRef, Table> tablesWithIds = new LinkedHashMap<>();
        Map<TableRef, List<ForeignKey>> referredKeys = new LinkedHashMap<>();

        // group by name (same name in multiple schemas)
        LinkedHashMap<String, List<TableRef>> seenNames = new LinkedHashMap<>();
        for (TableRef ref: tables.keySet()) {
            List<TableRef> fullNames = seenNames.get(ref.name());
            if (fullNames == null) {
                fullNames = new LinkedList<>();
                seenNames.put(ref.name(), fullNames);
            }
            fullNames.add(ref);

            // compile referred keys
            List<ForeignKey> reffk = tables.get(ref).foreignKeys();
            for (ForeignKey fk: reffk) {
                List<ForeignKey> reffks = referredKeys.get(fk.tableRef());
                if (reffks == null) {
                    reffks = new LinkedList<>();
                    referredKeys.put(fk.tableRef(), reffks);
                }
                reffks.add(fk);
            }
        }

        Collection<Table> unordered = new LinkedList<>();
        // set ids as either short (name) or long (schema.name)
        for (Map.Entry<String, List<TableRef>> e: seenNames.entrySet()) {
            if (e.getValue().size() > 1) {
                for (TableRef ref: e.getValue()) {
                    unordered.add(new Table(ref.asUnquotedIdentifier(), tables.get(ref), referredKeys.get(ref)));
                }
            } else {
                TableRef ref = e.getValue().get(0);
                unordered.add(new Table(e.getKey(), tables.get(ref), referredKeys.get(ref)));
            }
        }

        // order by table.id() - exposed table identifier which may or may not contain schema component
        Collection<Table> ordered = new TreeSet<>((o1, o2) -> {
            return o1.id().compareTo(o2.id());
        });
        ordered.addAll(unordered);

        for (Table t: ordered) {
            tablesWithIds.put(t.tableRef(), t);
        }
        return tablesWithIds;
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
        List<String> ret = new LinkedList<>();
        for (Table t: tables.values()) {
            ret.add(t.id());
        }
        return ret;
    }

    public Set<String> schemas() {
        return schemas;
    }

    public String defaultSchema() {
        return defaultSchema;
    }

    public Table tableById(String id) {
        return table(new TableRef(id));
    }

    public Table newTable(String schema, String name, List<Column> columns, PrimaryKey key, List<ForeignKey> foreignKeys) {
        Table table = new Table(schema, name, columns, key, foreignKeys);
        table.catalog(this);
        return table;
    }

    public Table newTable(Table table) {
        Table t = table.copy();
        t.catalog(this);
        return t;
    }

    /**
     * This kind of sorting is used by DROP which first has to
     * remove R that links to T before it can remove a T.
     *
     * @param tables
     * @return
     */
    public Set<Table> orderByReferred(Collection<Table> tables) {
        Set<Table> sorted = new TreeSet<>(new Comparator<Table>() {
            @Override
            public int compare(Table t1, Table t2) {
                if (t1 == t2) {
                    return 0;
                }
                if (transitivelyReferredBy(t1, t2)) {
                    return 1;
                }

                return -1;
            }
        });
        sorted.addAll(tables);
        return sorted;
    }

    private boolean transitivelyReferredBy(Table t1, Table t2) {
        for (ForeignKey fk: t1.referredKeys()) {
            Table dep = table(fk.columns().get(0).tableRef());
            if (dep.id().equals(t2.id())) {
                return true;
            }
            return transitivelyReferredBy(dep, t2);
        }
        return false;
    }

    /**
     * This kind of sorting is by CREATE table which first has
     * to create table T, and then table R that links to table T.
     *
     * @param tables
     * @return
     */
    public Set<Table> orderByReferring(Collection<Table> tables) {
        Set<Table> sorted = new TreeSet<>(new Comparator<Table>() {
            @Override
            public int compare(Table t1, Table t2) {
                if (t1 == t2) {
                    return 0;
                }
                if (transitivelyReferring(t1, t2)) {
                    return 1;
                }
                return -1;
            }
        });
        sorted.addAll(tables);
        return sorted;
    }

    private boolean transitivelyReferring(Table t1, Table t2) {
        for (ForeignKey fk: t1.foreignKeys()) {
            Table dep = table(fk.tableRef());
            if (dep.tableRef().equals(t2.tableRef())) {
                return true;
            }
            return transitivelyReferring(dep, t2);
        }
        return false;
    }
}
