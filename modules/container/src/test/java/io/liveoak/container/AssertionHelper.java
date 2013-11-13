package io.liveoak.container;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class AssertionHelper {

    public static interface AssertionBlock {
        public void doAssertions() throws Throwable;
    }

    public void assertThat(AssertionBlock b) {
        try {
            b.doAssertions();
        } catch (Throwable e) {
            System.err.println( "caught: " + e );
            errors.add(e);
        }
    }

    public void complete() throws Throwable {
        if (errors.isEmpty()) {
            return;
        }

        if (errors.get(0) instanceof AssertionError) {
            throw errors.get(0);
        }

        throw new AssertionError(errors.get(0));

    }

    private List<Throwable> errors = new ArrayList<>();
}
