/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql.sql;

import java.util.function.Consumer;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class ExpressionWalker {

    public void traverse(Operator o, Consumer<Expression> callback) {
        traverse(o.left(), callback);
        callback.accept(o);
        traverse(o.right(), callback);
    }

    public void traverse(Expression e, Consumer<Expression> callback) {
        if (e instanceof Operator) {
            traverse((Operator) e, callback);
        } else if (e != null) {
            callback.accept(e);
        }
    }
}
