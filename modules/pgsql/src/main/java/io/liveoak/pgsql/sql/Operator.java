package io.liveoak.pgsql.sql;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public abstract class Operator<T extends Operator> extends Expression {

    private Expression e1;
    private Expression e2;

    public Operator() {}

    public Operator(Expression e1) {
        this.e1 = e1;
    }

    public Operator(Expression e1, Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public abstract String name();

    public Expression left() {
        return e1;
    }

    public T left(Expression e) {
        e1 = e;
        return (T) this;
    }

    public Expression right() {
        return e2;
    }

    public T right(Expression e) {
        e2 = e;
        return (T) this;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        if (e1 != null) {
            boolean groupLeft = groupLeft();
            if (groupLeft) {
                sb.append("(");
            }
            sb.append(e1);
            if (groupLeft) {
                sb.append(")");
            }
        }
        if (e2 != null) {
            boolean groupRight = groupRight();
            sb.append(name());
            if (groupRight) {
                sb.append("(");
            }
            sb.append(e2);
            if (groupRight) {
                sb.append(")");
            }
        }
        return sb.toString();
    }

    protected boolean groupRight() {
        return !Not.class.isAssignableFrom(e2.getClass()) && e2 instanceof LogicalOperator && !getClass().isAssignableFrom(e2.getClass());
    }

    protected boolean groupLeft() {
        return !Not.class.isAssignableFrom(e1.getClass()) && e1 instanceof LogicalOperator && !getClass().isAssignableFrom(e1.getClass());
    }
}
