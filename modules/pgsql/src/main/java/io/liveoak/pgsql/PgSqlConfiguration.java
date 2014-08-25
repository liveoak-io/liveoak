package io.liveoak.pgsql;

import java.util.List;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public interface PgSqlConfiguration {

    public List<String> exposedSchemas();

    public List<String> blockedSchemas();

    public boolean allowCreateSchema();

    public String defaultSchema();

}
