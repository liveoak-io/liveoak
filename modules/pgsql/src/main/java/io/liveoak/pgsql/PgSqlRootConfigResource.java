package io.liveoak.pgsql;

import java.sql.Connection;
import java.sql.SQLException;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;
import org.postgresql.ds.PGPoolingDataSource;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlRootConfigResource extends DefaultRootResource {

    private static Logger log = Logger.getLogger(PgSqlRootConfigResource.class);
    private PGPoolingDataSource ds;

    public PgSqlRootConfigResource(String id) {
        super(id);
    }

    @Override
    public void stop() {
        PGPoolingDataSource ds = this.ds;
        this.ds = null;

        if ( ds != null ) {
            ds.close();
        }
    }

    /**
     * Users of the connection must make sure to call {@link java.sql.Connection#close()} when done using it,
     * so that it is returned to the pool.
     *
     * @return a connection retrieved from the pool
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        if (ds == null) {
            throw new IllegalStateException("DataSource not available");
        }
        return ds.getConnection();
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        PGPoolingDataSource ds = this.ds;
        sink.accept("server", ds.getServerName() );
        sink.accept("port", ds.getPortNumber());
        sink.accept("db", ds.getDatabaseName());
        sink.accept("user", ds.getUser());
        sink.accept("password", ds.getPassword());
        sink.accept("max-connections", ds.getMaxConnections());
        sink.accept("initial-connections", ds.getInitialConnections());
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

        String dbName = state.getPropertyAsString("db");
        if (dbName == null) {
            throw new InitializationException("Data base name not specified! Use \"db\": \"DATABASE_NAME\"");
        }

        String server = state.getPropertyAsString("server");
        if (server == null) {
            server = "localhost";
        }

        Integer port = state.getPropertyAsInteger("port");
        if (port == null) {
            port = 5432;
        }

        String user = state.getPropertyAsString("user");
        String pass = state.getPropertyAsString("password");

        Integer maxConnections = state.getPropertyAsInteger("max-connections");
        if (maxConnections == null) {
            maxConnections = 10;
        }

        Integer initialConnections = state.getPropertyAsInteger("initial-connections");
        if (initialConnections == null) {
            initialConnections = 1;
        }


        PGPoolingDataSource old = this.ds;
        boolean recreate = old == null
                || !dbName.equals(old.getDatabaseName())
                || !server.equals(old.getServerName())
                || !port.equals(old.getPortNumber())
                || !user.equals(old.getUser())
                || !pass.equals(old.getPassword())
                || !maxConnections.equals(old.getMaxConnections())
                || !initialConnections.equals(old.getInitialConnections());

        if (recreate) {
            // reinit ds with new settings
            PGPoolingDataSource nu = new PGPoolingDataSource();
            nu.setServerName(server);
            nu.setPortNumber(port);
            nu.setDatabaseName(dbName);
            nu.setUser(user);
            nu.setPassword(pass);
            nu.setMaxConnections(maxConnections);
            nu.setInitialConnections(initialConnections);
            nu.initialize();

            this.ds = nu;

            if (old != null) {
                try {
                    old.close();
                } catch (Exception e) {
                    log.debug("[IGNORED] Exception while closing the datasource: ", e);
                }
            }
        }

        responder.resourceUpdated(this);
    }
}
