package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class MockRootResource implements RootResource {

    private String id;

    private BlockingQueue<RequestContext> requests = new LinkedBlockingDeque<>();

    public MockRootResource(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        requests.add(ctx);
        sink.close();
    }

    public RequestContext pollRequest(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return requests.poll(timeout, timeUnit);
    }

}
