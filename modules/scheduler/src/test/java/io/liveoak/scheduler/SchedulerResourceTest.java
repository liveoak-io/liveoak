package io.liveoak.scheduler;

import io.liveoak.container.ReturnFieldsImpl;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scheduler.extension.SchedulerExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
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
    public void loadExtensions() throws Exception {
        loadExtension( "scheduler", new SchedulerExtension() );
    }

    @Test
    public void testAddTrigger() throws Exception {
        RequestContext requestContext = new RequestContext.Builder().returnFields(new ReturnFieldsImpl("*").withExpand("members")).build();

        ResourceState triggerState = new DefaultResourceState();
        triggerState.putProperty("cron", "* * * * * ?");
        System.err.println( "creating a trigger" );
        ResourceState returnedState = client.create(requestContext, "/testOrg/testApp/scheduler", triggerState);
        System.err.println( "created a trigger: " + returnedState );

        String id = returnedState.id();

        System.err.println( "Sleeping 2 seconds" );
        // intentional, to allow the job to fire some
        Thread.sleep(2000);

        System.err.println( "Fetching firings" );

        ResourceState fromCollection = client.read(requestContext, "/testOrg/testApp/scheduler/" + id);

        System.err.println( "Fetched: " + fromCollection );

        assertThat(fromCollection).isNotNull();
        assertThat(fromCollection.getPropertyNames()).hasSize(2);
        assertThat(fromCollection.getProperty("cron")).isEqualTo("* * * * * ?");
        assertThat(fromCollection.getProperty("state")).isEqualTo("normal");

        assertThat(fromCollection.members().size()).isGreaterThanOrEqualTo(2);
    }
}
