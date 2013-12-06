package io.liveoak.container.traversal;

import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class TraversalPlanTest {

    @Test
    public void testEmpty() {
        TraversalPlan plan = new TraversalPlan( RequestType.READ, new ResourcePath(""));

        assertThat( plan.steps() ).isEmpty();
    }

    @Test
    public void testSimpleRead() {
        TraversalPlan plan = new TraversalPlan( RequestType.READ, new ResourcePath("/foo/bar/baz" ) );

        assertThat( plan.steps() ).hasSize( 3 );
        assertThat( plan.steps().get(0)).isInstanceOf(ReadStep.class);
        assertThat( plan.steps().get(1)).isInstanceOf(ReadStep.class);
        assertThat( plan.steps().get(2)).isInstanceOf( ReadStep.class );
    }

    @Test
    public void testSimpleReadConfiguration() {
        TraversalPlan plan = new TraversalPlan( RequestType.READ, new ResourcePath("/foo/bar;config/baz" ) );

        assertThat( plan.steps() ).hasSize( 4 );
        assertThat( plan.steps().get(0)).isInstanceOf(ReadStep.class);
        assertThat( plan.steps().get(1)).isInstanceOf( ReadStep.class );
        assertThat( plan.steps().get(2)).isInstanceOf( ConfigurationStep.class );
        assertThat( plan.steps().get(3)).isInstanceOf( ReadStep.class );
    }

    @Test
    public void testSimpleDelete() {
        TraversalPlan plan = new TraversalPlan( RequestType.DELETE, new ResourcePath("/foo/bar/baz" ) );

        assertThat( plan.steps() ).hasSize( 4 );
        assertThat( plan.steps().get(0)).isInstanceOf(ReadStep.class);
        assertThat( plan.steps().get(1)).isInstanceOf(ReadStep.class);
        assertThat( plan.steps().get(2)).isInstanceOf( ReadStep.class );
        assertThat( plan.steps().get(3)).isInstanceOf( DeleteStep.class );
    }
}
