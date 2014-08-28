/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql.data;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class QueryResults {

    private List<String> columnNames;
    private List<Row> rows;

    public QueryResults() {
        this.columnNames = Collections.emptyList();
        this.rows = Collections.emptyList();
    }

    public QueryResults(List<String> columnNames, List<Row> rows) {
        this.columnNames = Collections.unmodifiableList(columnNames);
        this.rows = Collections.unmodifiableList(rows);
    }

    public List<String> columnNames() {
        return columnNames;
    }

    public List<Row> rows() {
        return rows;
    }

    public int count() {
        if (rows != null) {
            return rows.size();
        }
        return 0;
    }
}
