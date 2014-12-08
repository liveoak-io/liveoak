package io.liveoak.scheduler;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scheduler.extension.SchedulerExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractTestCaseWithTestApp;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class SchedulerResourceTest extends AbstractTestCaseWithTestApp {

    @BeforeClass
    public static void loadExtensions() throws Exception {
        loadExtension("scheduler", new SchedulerExtension());
        installTestAppResource("scheduler", "scheduler", JsonNodeFactory.instance.objectNode());
    }

    @Test
    public void testAddTrigger() throws Exception {
        RequestContext requestContext = new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build();

        ResourceState triggerState = new DefaultResourceState();
        triggerState.putProperty("cron", "* * * * * ?");
        System.err.println("creating a trigger");
        ResourceState returnedState = client.create(requestContext, "/testApp/scheduler", triggerState);
        System.err.println("created a trigger: " + returnedState);

        String id = returnedState.id();

        System.err.println("Sleeping 2 seconds");
        // intentional, to allow the job to fire some
        Thread.sleep(2000);

        System.err.println("Fetching firings");

        ResourceState fromCollection = client.read(requestContext, "/testApp/scheduler/" + id);

        System.err.println("Fetched: " + fromCollection);

        assertThat(fromCollection).isNotNull();
        assertThat(fromCollection.getPropertyNames()).hasSize(2);
        assertThat(fromCollection.getProperty("cron")).isEqualTo("* * * * * ?");
        assertThat(fromCollection.getProperty("state")).isEqualTo("normal");

        assertThat(fromCollection.members().size()).isGreaterThanOrEqualTo(2);
    }
}
