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
