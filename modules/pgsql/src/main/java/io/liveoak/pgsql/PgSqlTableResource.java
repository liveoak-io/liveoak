/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import io.liveoak.pgsql.data.QueryResults;
import io.liveoak.pgsql.data.Row;
import io.liveoak.pgsql.meta.Catalog;
import io.liveoak.pgsql.meta.Column;
import io.liveoak.pgsql.meta.QueryBuilder;

import io.liveoak.pgsql.meta.Table;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.Sorting;
import io.liveoak.spi.resource.MapResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlTableResource implements Resource {
    private static final String SCHEMA_ENDPOINT = ";schema";

    private PgSqlRootResource parent;
    private String id;
    private QueryResults results;
    private QueryBuilder queryBuilder;

    public PgSqlTableResource(PgSqlRootResource root, String table) {
        this.parent = root;
        this.id = table;
        this.queryBuilder = parent.queryBuilder();
    }

    @Override
    public PgSqlRootResource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {

        // perform select and store it for readMembers
        if (results != null) {
            sink.close();
            return;
        }
        results = queryTable(id, null, ctx);

        List<Resource> links = new LinkedList<>();
        MapResource batch = new MapResource();
        batch.put("rel", "schema");
        batch.put("href", uri() + SCHEMA_ENDPOINT);
        links.add(batch);
        sink.accept("links", links);

        sink.accept("count", results.count());
        sink.accept("type", "collection");
        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        for (Row row: results.rows()) {
            sink.accept(new PgSqlRowResource(this, row));
        }
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String childId, Responder responder) throws Exception {
        QueryResults results = queryTable(id, childId, ctx);

        if (results.count() == 0) {
            responder.noSuchResource( id );
        } else {
            responder.resourceRead(new PgSqlRowResource(this, results.rows().get(0)));
        }
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        // insert a new record into a table
        String itemId = state.id();
        if (itemId == null || itemId.length() == 0) {
            responder.invalidRequest("No id");
            return;
        }

        state.uri(new URI(uri().toString() + "/" + itemId));

        Catalog cat = parent.catalog();
        Table table = cat.tableById(id());

        try (Connection c = parent.connection()) {
            itemId = queryBuilder.executeCreate(ctx, c, table, state);

            // TODO: also handle expanded many-to-one / one-to-many
        }

        //readMember(ctx, itemId, responder);
        QueryResults results = queryTable(id, itemId, ctx);
        responder.resourceCreated(new PgSqlRowResource(this, results.rows().get(0)));
    }


    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        System.out.println("Table updateProperties");
        Resource.super.updateProperties(ctx, state, responder);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        Catalog cat = parent.catalog();
        Table t = cat.tableById(id);
        try (Connection c = parent.connection()) {
            queryBuilder.executeDeleteTable(c, t);
        }
        // trigger schema reload
        parent.reloadSchema();

        // TODO - does it make sense to return a body here at all? Container fails to set Content-Length if resourceDeleted(null)
        // only return id and uri in response - no members
        results = new QueryResults();
        responder.resourceDeleted(this);
    }

    public QueryResults queryResults() {
        return results;
    }

    public QueryResults queryTable(String table, String id, RequestContext ctx) throws SQLException, IOException {
        Catalog cat = parent.catalog();
        Table t = cat.tableById(table);
        try (Connection con = parent.connection()) {
            String q = ctx.resourceParams().value("q");

            if (id == null) {
                if (q != null) {
                    return queryBuilder.querySelectFromTable(con, t, replaceIdsWithColumnNames(ctx.sorting()), ctx.pagination(), q);
                } else {
                    return queryBuilder.querySelectFromTable(con, t, replaceIdsWithColumnNames(ctx.sorting()), ctx.pagination());
                }
            } else {
                return queryBuilder.querySelectFromTableWhereId(con, t, id);
            }
        }
    }

    private Sorting replaceIdsWithColumnNames(Sorting sorting) {
        if (sorting == null) {
            return null;
        }
        Catalog cat = parent.catalog();
        Sorting.Builder builder = new Sorting.Builder();
        for (Sorting.Spec f: sorting) {
            if (f.name().equals("id")) {
                Table table = cat.tableById(id);
                for (Column col: table.pk().columns()) {
                    builder.addSpec(col.name(), f.ascending());
                }
            } else {
                builder.addSpec(f.name(), f.ascending());
            }
        }
        return builder.build();
    }
}
