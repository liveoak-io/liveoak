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
