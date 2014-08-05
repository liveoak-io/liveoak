package io.liveoak.pgsql.sql;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GreaterThan extends RelationalOperator {

    public GreaterThan() {}

    public GreaterThan(RelationalOperand e1, RelationalOperand e2) {
        super(e1, e2);
    }

    @Override
    public String name() {
        return ">";
    }
}
