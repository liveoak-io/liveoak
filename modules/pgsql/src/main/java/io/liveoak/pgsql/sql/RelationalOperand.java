package io.liveoak.pgsql.sql;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public abstract class RelationalOperand extends Expression {

    public Equal equalTo(RelationalOperand e) {
        return new Equal(this, e);
    }

    public NotEqual notEqualTo(RelationalOperand e) {
        return new NotEqual(this, e);
    }

    public GreaterThan greaterThan(RelationalOperand e) {
        return new GreaterThan(this, e);
    }

    public GreaterThanOrEqual greaterThanOrEqual(RelationalOperand e) {
        return new GreaterThanOrEqual(this, e);
    }

    public LessThan lessThan(RelationalOperand e) {
        return new LessThan(this, e);
    }

    public LessThanOrEqual lessThanOrEqual(RelationalOperand e) {
        return new LessThanOrEqual(this, e);
    }
}
