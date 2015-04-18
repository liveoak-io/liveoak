package io.liveoak.container;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.Services;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.apache.http.HttpResponse;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Ken Finnigan
 */
public class AbstractContainerTest {

    protected static LiveOakSystem system;

    protected static Consumer<ServiceTarget> preWaitSetupConsumer() {
        return AbstractContainerTest::createMocks;
    }

    protected static void setupMocks() {
        createMocks(system.serviceTarget());
    }

    private static void createMocks(ServiceTarget target) {
        target.addService(Services.SECURITY_CLIENT, new ValueService<>(new ImmediateValue<>(new MockSecurityClient()))).install();
        target.addService(Services.SECURITY_DIRECT_ACCESS_CLIENT, new ValueService<>(new ImmediateValue<>(new MockDirectAccessClient()))).install();
    }

    protected static boolean awaitStability() throws InterruptedException {
        // Default all calls to a 7 second timeout if not specified
        return awaitStability(7, TimeUnit.SECONDS);
    }

    protected static boolean awaitStability(int timeout, TimeUnit unit) throws InterruptedException {
        return awaitStability(timeout, unit, new HashSet<>(), new HashSet<>());
    }

    protected static boolean awaitStability(long timeout, TimeUnit unit, Set<? super ServiceController<?>> failed, Set<? super ServiceController<?>> problem) throws InterruptedException {
        boolean stable = system.awaitStability(timeout, unit, failed, problem);
        if (!stable) {
            log.warn("awaitStability() may require an increased timeout duration.");
        }

        if (failed != null && !failed.isEmpty()) {
            Iterator<? super ServiceController<?>> failedIterator = failed.iterator();
            while (failedIterator.hasNext()) {
                ServiceController controller = (ServiceController) failedIterator.next();
                log.errorf(CONTROLLER_MESSAGE, controller.getName(), controller.getState(), controller.getSubstate(), controller.getMode());
                if (controller.getStartException() != null) {
                    controller.getStartException().printStackTrace();
                }
            }
        }

        if (problem != null && !problem.isEmpty()) {
            Iterator<? super ServiceController<?>> problemIterator = problem.iterator();
            while (problemIterator.hasNext()) {
                ServiceController controller = (ServiceController) problemIterator.next();
                log.errorf(CONTROLLER_MESSAGE, controller.getName(), controller.getState(), controller.getSubstate(), controller.getMode());
                if (controller.getStartException() != null) {
                    controller.getStartException().printStackTrace();
                }
            }
        }

        return stable;
    }

    protected ResourceState decode(HttpResponse response) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        response.getEntity().writeTo(out);
        out.flush();
        out.close();
        System.err.println("========= HttpResponse ==========");
        System.err.println(buffer.toString(Charset.defaultCharset()));
        System.err.println("===================");
        return system.codecManager().decode(MediaType.JSON, buffer);
    }

    private static String CONTROLLER_MESSAGE = "Controller %s is in State: %s, Substate: %s and Mode: %s";

    private static final Logger log = Logger.getLogger(AbstractContainerTest.class);
}
