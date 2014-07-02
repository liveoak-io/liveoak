package io.liveoak.pgsql;

import java.sql.Connection;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import io.liveoak.spi.resource.async.PropertySink;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlRootResource extends DefaultRootResource {

    private final PgSqlRootConfigResource configResource;

    public PgSqlRootResource(String id) {
        super(id);
        configResource = new PgSqlRootConfigResource(id);
    }

    public PgSqlRootConfigResource configuration() {
        return configResource;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("name", "PgSqlRootResource");
        sink.accept("version", "1.0");
        sink.close();
    }

}
