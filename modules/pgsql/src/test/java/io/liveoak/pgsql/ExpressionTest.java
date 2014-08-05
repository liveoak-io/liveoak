package io.liveoak.pgsql;

import io.liveoak.pgsql.sql.And;
import io.liveoak.pgsql.sql.Operator;
import io.liveoak.pgsql.sql.Expression;
import io.liveoak.pgsql.sql.Identifier;
import io.liveoak.pgsql.sql.Or;
import io.liveoak.pgsql.sql.Value;
import org.fest.assertions.Assertions;
import org.junit.Test;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class ExpressionTest {

    @Test
    public void test() {
        // basic expression
        Identifier id = new Identifier("total");
        Value val = new Value(42);
        Operator eq1 = id.equalTo(val);

        Assertions.assertThat(eq1.toString()).isEqualTo("total=?");

        Identifier id2 = new Identifier("item.total");
        Operator eq2 = id.equalTo(id2);

        Assertions.assertThat(eq2.toString()).isEqualTo("total=item.total");

        // basic AND expression
        Operator eq3 = new Identifier("country").equalTo(new Value("US"));
        Expression eq1AndEq3 = eq1.and(eq3);

        Assertions.assertThat(eq1AndEq3.toString()).isEqualTo("total=? AND country=?");

        // multiple AND expressions - no brackets required
        Expression eq2AndEq1AndEq3 = eq2.and(eq1AndEq3);

        Assertions.assertThat(eq2AndEq1AndEq3.toString()).isEqualTo("total=item.total AND total=? AND country=?");

        // multiple OR expressions - no brackets required
        Expression eq1Oreq2Oreq3 = eq1.or(eq2.or(eq3));

        Assertions.assertThat(eq1Oreq2Oreq3.toString()).isEqualTo("total=? OR total=item.total OR country=?");

        // order does not matter - end result is the same
        eq1Oreq2Oreq3 = eq1.or(eq2).or(eq3);
        Assertions.assertThat(eq1Oreq2Oreq3.toString()).isEqualTo("total=? OR total=item.total OR country=?");


        // a mix of AND and OR expressions
        Expression eq1AndEq2 = eq1.and(eq2);
        Expression andOrAnd = eq1AndEq3.or(eq1AndEq2);

        // AND takes precedence so brackets are unnecessary but makes it easier to understand
        Assertions.assertThat(andOrAnd.toString()).isEqualTo("(total=? AND country=?) OR (total=? AND total=item.total)");


        // passing OR expression to an AND expression requires brackets
        And andex = eq1.or(eq2).and(eq3.or(eq2));
        Assertions.assertThat(andex.toString()).isEqualTo("(total=? OR total=item.total) AND (country=? OR total=item.total)");

        Or orex = eq1.or(eq2.and(eq3.or(eq2)));
        Assertions.assertThat(orex.toString()).isEqualTo("total=? OR (total=item.total AND (country=? OR total=item.total))");
    }
}
