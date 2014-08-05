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

    @Override
    public String toString() {
        return "?";
    }
}
