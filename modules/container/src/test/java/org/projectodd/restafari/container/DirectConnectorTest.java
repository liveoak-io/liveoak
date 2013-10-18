package org.projectodd.restafari.container;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.fest.assertions.Assertions.*;

/**
 * @author Bob McWhirter
 */
public class DirectConnectorTest {


    private AssertionHelper assertionHelper = new AssertionHelper();
    private DefaultContainer container;
    private DirectConnector connector;


    @Before
    public void setUp() throws Exception {
        this.container = new DefaultContainer();
        this.connector = this.container.directConnector();
    }

    @After
    public void checkAssertions() throws Throwable {
        this.assertionHelper.complete();
    }

    protected void assertions(AssertionHelper.AssertionBlock b) {
        this.assertionHelper.assertThat(b);
    }

    @Test
    public void testConnectorRead() throws Throwable {

        CountDownLatch latch = new CountDownLatch(2);

        this.connector.read("/", (response) -> {
            System.err.println("response to slash: " + response);
            assertions(() -> {
                assertThat(response.responseType()).isEqualTo(ResourceResponse.ResponseType.READ);
            });
            latch.countDown();
        });

        this.connector.read("/tacos", (response) -> {
            System.err.println("response to tacos: " + response);
            assertions(() -> {
                assertThat(response.responseType()).isEqualTo(ResourceResponse.ResponseType.ERROR);
            });
            latch.countDown();
        });

        latch.await();
    }
}
