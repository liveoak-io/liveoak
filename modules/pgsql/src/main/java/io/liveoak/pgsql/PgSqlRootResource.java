package io.liveoak.pgsql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.liveoak.pgsql.meta.Catalog;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlRootResource extends DefaultRootResource {

    private static final String TABLE_NAMES = "pg.table.names";

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
    public Connection getConnection() throws SQLException {
        return configResource.getConnection();
    }

    public Catalog getCatalog() {
        return configResource.getCatalog();
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        //sink.accept("name", "PgSqlRootResource");
        //sink.accept("version", "1.0");
        //sink.close();

        // determine table names
        List<String> tables = listTables();

        // store to ctx attributes to pass on to readMembers
        ctx.requestAttributes().setAttribute(TABLE_NAMES, tables);

        // here only set num of tables as size
        sink.accept("count", tables.size());

        // maybe some other things to do with db as a whole
        sink.accept("type", "database");
        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        List<String> tables = (List<String>) ctx.requestAttributes().getAttribute(TABLE_NAMES);
        for (String table: tables) {
            sink.accept(new PgSqlTableResource(this, table));
        }
        sink.close();
    }

    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        List<String> tables = listTables();
        int pos = tables.indexOf(id);
        if (pos == -1) {
            responder.noSuchResource( id );
        } else {
            responder.resourceRead(new PgSqlTableResource(this, tables.get(pos)));
        }
    }

    public List<String> listTables() throws SQLException {
        return getCatalog().tableIds();
    }
}