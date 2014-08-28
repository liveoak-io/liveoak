/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql.sql;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GreaterThanOrEqual extends RelationalOperator {

    public GreaterThanOrEqual() {}

    public GreaterThanOrEqual(RelationalOperand e1, RelationalOperand e2) {
        super(e1, e2);
    }

    @Override
    public String name() {
        return ">=";
    }
}
