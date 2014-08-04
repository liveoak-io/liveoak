package io.liveoak.pgsql;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

import io.liveoak.pgsql.meta.Catalog;
import io.liveoak.pgsql.meta.QueryBuilder;
import io.liveoak.pgsql.meta.Table;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlBatchResource implements Resource {

    private PgSqlRootResource parent;
    private String id;

    public PgSqlBatchResource(PgSqlRootResource parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        // check the requested action
        String action = ctx.resourceParams().value("action");
        if (action == null || (!action.equals("create") && !action.equals("update") && !action.equals("delete"))) {
            responder.invalidRequest("'action' parameter needs to be specified with one of: 'create', 'update', 'delete', as a value");
            return;
        }

        List<ResourcePath.Segment> thisPath = ctx.resourcePath().segments();

        // for delete operation uris that identify tables require special handling
        // as we have to delete them in order of dependencies
        List<Table> deleteList = new LinkedList<>();

        Catalog cat = parent.getCatalog();
        try (Connection c = parent.getConnection()) {
            // iterate through members one by one, and perform operation on each
            for (ResourceState member : state.members()) {
                // parse uri
                ResourcePath uri = new ResourcePath(member.uri().toString());

                List<ResourcePath.Segment> pathSegments = uri.segments();
                if (pathSegments.size() != 4 && pathSegments.size() != 3) {
                    throw new IllegalArgumentException("Uri out of scope: " + uri);
                }
                if (!pathSegments.get(0).name().equals(thisPath.get(0).name())) {
                    throw new IllegalArgumentException("Uri out of scope of current application (" + thisPath.get(0) + "): " + uri);
                }
                if (!pathSegments.get(1).name().equals(thisPath.get(1).name())) {
                    throw new IllegalArgumentException("Uri out of scope of current service (" + thisPath.get(1) + "): " + uri);
                }

                String tableName = pathSegments.get(2).name();
                if (tableName.equals(this.id)) {
                    continue;
                }
                Table table = cat.tableById(tableName);
                if (table == null) {
                    throw new IllegalArgumentException("Table not found: " + tableName + " (uri: " + uri + ")");
                }

                if (pathSegments.size() == 4) {
                    String itemId = pathSegments.get(3).name();

                    if (action.equals("create")) {
                        new QueryBuilder(cat).executeInsert(ctx, c, table, member);
                    } else if (action.equals("delete")) {
                        new QueryBuilder(cat).executeDelete(ctx, c, table, itemId, ctx.resourceParams().contains("cascade"));
                    } else if (action.equals("update")) {
                        new QueryBuilder(cat).executeUpdate(ctx, c, table, member);
                    }
                } else {
                    if (action.equals("delete")) {
                        deleteList.add(table);
                    } else {
                        responder.invalidRequest("'action' parameter value not supported for collection uri (" + uri + "): " + action);
                        return;
                    }
                }

                // TODO: also handle expanded many-to-one / one-to-many
            }
            if (deleteList.size() > 0) {
                new QueryBuilder(cat).executeDeleteTables(c, deleteList);
                // trigger schema reload
                parent.configuration().reloadSchema();
            }
        }
        responder.resourceRead(this);
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        responder.noSuchResource(id);
    }

}
