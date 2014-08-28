/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql.sql;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public abstract class RelationalOperator<T extends RelationalOperator> extends Operator<T> {

    public RelationalOperator(RelationalOperand e1, RelationalOperand e2) {
        super(e1, e2);
    }

    public RelationalOperator() {
    }
}
