/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql.sql;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public abstract class Expression {

    public And and(Expression e) {
        return new And(this, e);
    }

    public Or or(Expression e) {
        return new Or(this, e);
    }

    public Not not() {
        return new Not(this);
    }

    public Expression normalize() {
        return this;
    }
}
