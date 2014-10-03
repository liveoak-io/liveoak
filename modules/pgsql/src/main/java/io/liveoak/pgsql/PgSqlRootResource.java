/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.liveoak.pgsql.meta.Catalog;
import io.liveoak.pgsql.meta.QueryBuilder;
import io.liveoak.pgsql.meta.Table;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.exceptions.ResourceProcessingException;
import io.liveoak.spi.resource.MapResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.DefaultRootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlRootResource extends DefaultRootResource implements SynchronousResource {

    private static final String TABLE_NAMES = "pg.table.names";

    private static final String BATCH_ENDPOINT = "_batch";

    private final PgSqlRootConfigResource configResource;

    public PgSqlRootResource(String id) {
        super(id);
        configResource = new PgSqlRootConfigResource(id);
    }

    public PgSqlRootConfigResource configuration() {
        return configResource;
    }

    /**
     * Users of the connection must make sure to call {@link java.sql.Connection#close()} when done using it,
     * so that it is returned to the pool.
     *
     * @return a connection retrieved from the pool
     * @throws SQLException
     */
    public Connection connection() throws SQLException {
        return configResource.connection();
    }

    public Catalog catalog() {
        return configResource.catalog();
    }

    public QueryBuilder queryBuilder() {
        return configResource.queryBuilder();
    }

    public PgSqlCRUDController controller() {
        return configResource.controller();
    }

    public void reloadSchema() throws SQLException {
        configResource.reloadSchema();
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {

        // determine table names
        List<String> tables = catalog().tableIds();

        // store to ctx attributes to pass on to readMembers
        ctx.requestAttributes().setAttribute(TABLE_NAMES, tables);

        List<Resource> links = new LinkedList<>();
        MapResource batch = new MapResource();
        batch.put("rel", "batch");
        batch.put(LiveOak.HREF, uri() + "/" + BATCH_ENDPOINT);
        links.add(batch);

        Map<String, Object> result = new HashMap<>();
        result.put("links", links);

        // here only set num of tables as size
        result.put("count", tables.size());

        // maybe some other things to do with db as a whole
        result.put("type", "database");
        return result;
    }

    @Override
    public Resource member(RequestContext ctx, String id) throws Exception {
        if (BATCH_ENDPOINT.equals(id)) {
            return new PgSqlBatchResource(this, BATCH_ENDPOINT);
        }

        String tableId = id;
        boolean schemaReq = false;

        ResourcePath.Segment tail = ctx.resourcePath() != null ? ctx.resourcePath().tail() : null;
        if (tail != null && tail.matrixParameters().containsKey("schema")) {
            tableId = tail.name();
            schemaReq = true;
        }
        List<String> tables = catalog().tableIds();
        int pos = tables.indexOf(tableId);
        if (pos == -1) {
            return null;
        } else if (schemaReq) {
            return new PgSqlTableSchemaResource(this, tail.name());
        } else {
            return new PgSqlTableResource(this, tables.get(pos));
        }
    }

    @Override
    public Collection<Resource> members(RequestContext ctx) throws Exception {
        List<String> tables = (List<String>) ctx.requestAttributes().getAttribute(TABLE_NAMES);
        return tables.stream()
                .map(table -> new PgSqlTableResource(this, table))
                .collect(Collectors.toList());
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

        Table table;
        try {
            table = controller().parseCreateTableRequest(state, true);
        } catch(ResourceProcessingException e) {
            responder.error(e.errorType(), e.getMessage(), e.getCause());
            return;
        }

        // execute ddl
        try (Connection c = connection()) {
            queryBuilder().executeCreateTable(c, table);
        }

        // trigger schema reread
        reloadSchema();

        responder.resourceRead(new PgSqlTableSchemaResource(this, catalog().table(table.tableRef()).id()));
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        // this is a way to trigger schema refresh from db
        super.updateProperties(ctx, state, responder);
    }
}