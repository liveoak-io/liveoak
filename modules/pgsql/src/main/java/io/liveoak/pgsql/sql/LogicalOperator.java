package io.liveoak.pgsql.sql;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public abstract class LogicalOperator<T extends LogicalOperator> extends Operator<T> {

    public LogicalOperator() {
    }

    public LogicalOperator(Expression e) {
        super(e);
    }

    public LogicalOperator(Expression e1, Expression e2) {
        super(e1, e2);
    }

    public abstract T next(Expression e);

    @Override
    public Expression normalize() {
        if (right() == null) {
            return left().normalize();
        }
        return super.normalize();
    }
}
