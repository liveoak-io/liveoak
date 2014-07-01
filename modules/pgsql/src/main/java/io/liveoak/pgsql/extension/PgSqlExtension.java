package io.liveoak.pgsql.extension;

import io.liveoak.pgsql.PgSqlRootResource;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {

    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        PgSqlRootResource publicResource = new PgSqlRootResource( context.resourceId() );
        context.mountPublic( publicResource );
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
