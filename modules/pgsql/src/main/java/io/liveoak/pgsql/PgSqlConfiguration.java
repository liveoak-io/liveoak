/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql;

import java.util.List;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public interface PgSqlConfiguration {

    public List<String> exposedSchemas();

    public List<String> blockedSchemas();

    public boolean allowCreateSchema();

    public boolean includeTotalCount();

    public String defaultSchema();

}
