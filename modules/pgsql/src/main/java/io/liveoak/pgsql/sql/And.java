/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql.sql;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class And extends LogicalOperator<And> {

    public And() {}

    public And(Expression e) {
        super(e);
    }

    public And(Expression e1, Expression e2) {
        super(e1, e2);
    }

    public And next(Expression e) {
        if (left() == null) {
            left(e);
            return this;
        }

        Expression current = right();
        if (current == null) {
            right(e);
            return this;
        }
        And next = new And(current, e);
        right(next);
        return next;
    }

    @Override
    public String name() {
        return " AND ";
    }
}
