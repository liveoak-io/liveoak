package io.liveoak.scheduler;

import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class SchedulerResourceTest extends AbstractResourceTestCase {
    @Override
    public RootResource createRootResource() {
        return new SchedulerResource( "scheduler" );
    }

    @Test
    public void testAddTrigger() throws Exception {
        RequestContext requestContext = new RequestContext.Builder().build();

        ResourceState triggerState = new DefaultResourceState();
        triggerState.putProperty( "cron", "* * * * * ?");
        ResourceState returnedState = connector.create( requestContext, "/scheduler", triggerState );

        String id = returnedState.id();

        System.err.println( returnedState );

        ResourceState fromCollection = connector.read( requestContext, "/scheduler/" + id );

        assertThat( fromCollection ).isNotNull();
        assertThat( fromCollection.getPropertyNames()).hasSize( 2 );
        assertThat( fromCollection.getProperty( "cron" ) ).isEqualTo( "* * * * * ?" );
        assertThat( fromCollection.getProperty( "state" ) ).isEqualTo( "normal" );
    }

}
