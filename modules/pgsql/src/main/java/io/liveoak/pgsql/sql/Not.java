/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql.sql;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class Not extends LogicalOperator<Not> {

    public Not() {}

    public Not(Expression e) {
        next(e);
    }

    @Override
    public Not next(Expression e) {
        right(e);
        return this;
    }

    @Override
    public String name() {
        return "NOT ";
    }
}
