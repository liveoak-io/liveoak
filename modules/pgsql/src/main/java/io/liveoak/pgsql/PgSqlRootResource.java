package io.liveoak.pgsql;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import io.liveoak.spi.resource.async.PropertySink;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlRootResource extends DefaultRootResource {

    public PgSqlRootResource(String id) {
        super(id);
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("name", "PgSqlRootResource");
        sink.accept("version", "1.0");
        sink.close();
    }

}
