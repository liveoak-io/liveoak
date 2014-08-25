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
