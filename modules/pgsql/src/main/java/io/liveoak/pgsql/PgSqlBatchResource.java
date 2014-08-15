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

    private static final String CREATE = "create";
    private static final String UPDATE = "update";
    private static final String DELETE = "delete";

    private PgSqlRootResource parent;
    private String id;
    private QueryBuilder queryBuilder;

    public PgSqlBatchResource(PgSqlRootResource parent, String id) {
        this.parent = parent;
        this.id = id;
        this.queryBuilder = parent.queryBuilder();
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
        if (action == null || (!action.equals(CREATE) && !action.equals(UPDATE) && !action.equals(DELETE))) {
            responder.invalidRequest("'action' parameter needs to be specified with one of: 'create', 'update', 'delete', as a value");
            return;
        }

        List<ResourcePath.Segment> thisPath = ctx.resourcePath().segments();

        // for delete operation uris that identify tables require special handling
        // as we have to delete them in order of dependencies
        List<Table> workList = new LinkedList<>();

        Catalog cat = parent.catalog();
        try (Connection c = parent.connection()) {
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

                if (pathSegments.size() == 4) {
                    if (table == null) {
                        throw new IllegalArgumentException("Table not found: " + tableName + " (uri: " + uri + ")");
                    }
                    String itemId = pathSegments.get(3).name();

                    if (action.equals(CREATE)) {
                        queryBuilder.executeInsert(ctx, c, table, member);
                    } else if (action.equals(DELETE)) {
                        queryBuilder.executeDelete(ctx, c, table, itemId, ctx.resourceParams().contains("cascade"));
                    } else if (action.equals(UPDATE)) {
                        queryBuilder.executeUpdate(ctx, c, table, member);
                    }
                } else {
                    if (action.equals(DELETE)) {
                        workList.add(table);
                    } else if (action.equals(CREATE)) {
                        workList.add(parent.controller().parseCreateTableRequest(member, false));
                    } else {
                        responder.invalidRequest("'action' parameter value not supported for collection uri (" + uri + "): " + action);
                        return;
                    }
                }

                // TODO: also handle expanded many-to-one / one-to-many
            }
            if (workList.size() > 0) {
                if (action.equals(DELETE)) {
                    queryBuilder.executeDeleteTables(c, workList);
                } else if (action.equals(CREATE)) {
                    queryBuilder.executeCreateTables(c, workList);
                }

                // trigger schema reload
                parent.reloadSchema();
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
