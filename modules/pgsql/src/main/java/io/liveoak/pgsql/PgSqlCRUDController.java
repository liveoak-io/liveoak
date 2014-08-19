package io.liveoak.pgsql;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.liveoak.pgsql.meta.Catalog;
import io.liveoak.pgsql.meta.Column;
import io.liveoak.pgsql.meta.ForeignKey;
import io.liveoak.pgsql.meta.PrimaryKey;
import io.liveoak.pgsql.meta.Table;
import io.liveoak.pgsql.meta.TableRef;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceProcessingException;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlCRUDController {

    private Catalog catalog;
    private PgSqlConfiguration configuration;

    public PgSqlCRUDController(Catalog catalog, PgSqlConfiguration configuration) {
        this.catalog = catalog;
        this.configuration = configuration;
    }

    public Table parseCreateTableRequest(ResourceState state, boolean checkDependencies) throws ResourceProcessingException {

        String id = state.id();
        if (id == null || id.length() == 0) {
            throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE, "No id");
        }

        // remove any matrix parameters if present
        // (response to /app/pgsql/table;schema for example produces 'id': 'table;schema' in the output,
        // and we want to properly process such response JSON as POST input)
        id = new ResourcePath(id).tail().toString();

        TableRef tableRef = new TableRef(id);

        // check if table exists
        Table table = catalog.table(tableRef);
        if (table != null) {
            throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.RESOURCE_ALREADY_EXISTS);
        }

        // check if schema is exposed
        String schema = tableRef.schema();
        if (schema == null) {
            schema = catalog.defaultSchema();
            tableRef = new TableRef(schema, tableRef.name());
        }

        Set<String> seenNames = new HashSet<>();
        List<Column> cols = new LinkedList<>();
        List columns = state.getPropertyAsList("columns");
        if (columns == null) {
            throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                    "Invalid JSON message - 'columns' missing");
        }
        for (Object o: columns) {
            if (o instanceof ResourceState == false) {
                throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                        "Invalid JSON message - 'columns' item not an object: " + o);
            }
            Column col = column(tableRef, (ResourceState) o);
            seenNames.add(col.name());
            cols.add(col);
        }

        PrimaryKey pk = null;
        List pkCols = state.getPropertyAsList("primary-key");
        if (pkCols != null) {
            List<Column> pkColumns = new LinkedList<>();

            for (Object pkCol: pkCols) {
                if (pkCol instanceof String == false) {
                    throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                            "Invalid JSON message - 'primary-key' item must be a string: " + pkCol);
                }
                if (!seenNames.contains(pkCol)) {
                    throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                            "Invalid JSON message - 'primary-key' refers to non-existent column: " + pkCol);
                }
                for (Column col: cols) {
                    if (col.name().equals(pkCol)) {
                        pkColumns.add(col);
                        break;
                    }
                }
            }
            pk = new PrimaryKey(pkColumns);
        } else {
            throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                    "Invalid JSON message - 'primary-key' field missing");
        }

        List<ForeignKey> fks = new LinkedList<>();
        List fkList = state.getPropertyAsList("foreign-keys");

        if (fkList != null) {
            for (Object o: fkList) {
                if (o instanceof ResourceState == false) {
                    throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                            "Invalid JSON message - 'foreign-keys' item not an object: " + o);
                }
                ResourceState item = (ResourceState) o;
                String fkTableId = item.getPropertyAsString("table");
                if (fkTableId == null) {
                    throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                            "Invalid JSON message - 'table' property missing on 'foreign-keys' item: " + o);
                }

                TableRef fkTableRef = new TableRef(fkTableId);
                // make sure the referred table exists
                Table fkTable = catalog.table(fkTableRef);
                if (checkDependencies && fkTable == null) {
                    throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                            "Table referred to by 'foreign-keys' item does not exist or is not accessible: " + fkTableId);
                }

                List fkcols = item.getPropertyAsList("columns");
                if (fkcols == null) {
                    throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                            "Invalid JSON message - 'columns' property missing on 'foreign-keys' item: " + o);
                }

                List<Column> fkColumns = new LinkedList<>();
                for (Object fkcol: fkcols) {
                    if (fkcol instanceof String == false) {
                        throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                                "Invalid JSON message - 'foreign-keys' / 'columns' item must be a string: " + fkcol);
                    }
                    if (!seenNames.contains(fkcol)) {
                        throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                                "Invalid JSON message - 'foreign-keys' / 'columns' item refers to non-existent column: " + fkcol);
                    }
                    for (Column col: cols) {
                        if (col.name().equals(fkcol)) {
                            fkColumns.add(col);
                            break;
                        }
                    }
                }

                // check that fk table pk column count and types match fk columns
                if (checkDependencies) {
                    Iterator<Column> it = fkTable.pk().columns().iterator();
                    Iterator<Column> fkit = fkColumns.iterator();
                    while (fkit.hasNext() && it.hasNext()) {
                        Column fkcol = fkit.next();
                        Column pkcol = it.next();
                        if (!fkcol.typeSpec().equals(pkcol.typeSpec())) {
                            throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                                    "Invalid JSON message - 'foreign-keys' / 'columns' type spec mismatch: " + fkcol.typeSpec() + " vs. " + pkcol.typeSpec());
                        }
                    }
                    if (it.hasNext() || fkit.hasNext()) {
                        throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                                "Invalid JSON message - 'foreign-keys' / 'columns' mismatch for: " + o);
                    }
                    fks.add(new ForeignKey(fkColumns, new TableRef(fkTable.schema(), fkTable.name())));
                } else {
                    fks.add(new ForeignKey(fkColumns, fkTableRef));
                }
            }
        }

        // check if schema exists. if it doesn't, check that config allows us to create a new schema
        // also check if the schema we're trying to create isn't deliberately hidden in config
        boolean createNewSchema = false;
        if (!catalog.schemas().contains(tableRef.schema())) {
            List<String> exposed = configuration.exposedSchemas();
            List<String> blocked = configuration.blockedSchemas();
            createNewSchema = (blocked == null || !blocked.contains(tableRef.schema())) &&
                    (exposed == null || exposed.isEmpty() || exposed.contains(tableRef.schema()));

            if (!createNewSchema || !configuration.allowCreateSchema()) {
                throw new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                        "Not allowed to create a new schema");
            }
        }

        return catalog.newTable(tableRef.schema(), tableRef.name(), cols, pk, fks);
    }

    private Column column(TableRef table, ResourceState col) {
        String name = col.getPropertyAsString("name");
        String type = col.getPropertyAsString("type");
        Integer size = col.getPropertyAsInteger("size");
        if (size == null) {
            size = -1;
        }
        Boolean nullable = col.getPropertyAsBoolean("nullable");
        if (nullable == null) {
            nullable = Boolean.TRUE;
        }
        Boolean unique = col.getPropertyAsBoolean("unique");
        if (unique == null) {
            unique = Boolean.FALSE;
        }
        return new Column(table, name, type, size, !nullable, unique);
    }
}
