package io.liveoak.pgsql.sql;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class Or extends LogicalOperator<Or> {

    public Or() {}

    public Or(Expression e) {
        super(e);
    }

    public Or(Expression e1, Expression e2) {
        super(e1, e2);
    }

    public Or next(Expression e) {
        if (left() == null) {
            left(e);
            return this;
        }

        Expression current = right();
        if (current == null) {
            right(e);
            return this;
        }

        Or next = new Or(current, e);
        right(next);
        return next;
    }

    @Override
    public String name() {
        return " OR ";
    }
}
