/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql.sql;

import io.liveoak.pgsql.meta.Column;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class Value extends RelationalOperand {

    private final Object value;

    public Value(Object val) {
        this.value = val;
    }

    public Object value() {
        return value;
    }

    @Override
    public String toString() {
        return "?";
    }
}
