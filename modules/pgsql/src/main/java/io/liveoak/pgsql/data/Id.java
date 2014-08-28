/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql.data;

import io.liveoak.pgsql.meta.Column;
import io.liveoak.pgsql.meta.PrimaryKey;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class Id {

    private PrimaryKey pk;
    private String [] id;

    public Id(PrimaryKey pk, String id) {
        this.pk = pk;
        this.id = PrimaryKey.splitId(id);
    }

    public String valueForColumn(String name) {
        int i = 0;
        for (Column c: pk.columns()) {
            if (c.name().equals(name)) {
                return id[i];
            }
            i++;
        }
        return null;
    }

    public String valueForIndex(int idx) {
        return id[idx];
    }
}
