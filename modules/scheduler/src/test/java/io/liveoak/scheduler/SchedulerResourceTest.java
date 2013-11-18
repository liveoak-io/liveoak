package io.liveoak.scheduler;

import io.liveoak.container.ReturnFieldsImpl;
import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.After;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class SchedulerResourceTest extends AbstractResourceTestCase {
    @Override
    public RootResource createRootResource() {
        return new SchedulerResource("scheduler");
    }

    @After
    public void stopScheduler() {
        //this.resource.destroy();
    }

    @Test
    public void testAddTrigger() throws Exception {
        RequestContext requestContext = new RequestContext.Builder().returnFields(new ReturnFieldsImpl("*").withExpand("members")).build();

        ResourceState triggerState = new DefaultResourceState();
        triggerState.putProperty("cron", "* * * * * ?");
        ResourceState returnedState = connector.create(requestContext, "/scheduler", triggerState);

        String id = returnedState.id();

        // intentional, to allow the job to fire some
        Thread.sleep(2000);

        ResourceState fromCollection = connector.read(requestContext, "/scheduler/" + id);

        assertThat(fromCollection).isNotNull();
        assertThat(fromCollection.getPropertyNames()).hasSize(2);
        assertThat(fromCollection.getProperty("cron")).isEqualTo("* * * * * ?");
        assertThat(fromCollection.getProperty("state")).isEqualTo("normal");

        assertThat( fromCollection.members().size() ).isGreaterThanOrEqualTo( 2 );
    }

}
