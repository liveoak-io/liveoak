package io.liveoak.container.extension.reconfig;

import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;

import java.util.concurrent.CountDownLatch;

/**
 * @author Bob McWhirter
 */
public class MockResponder implements Responder {

    private final CountDownLatch latch;
    public Resource resourceUpdated;
    public Resource resourceRead;
    public Resource resourceCreated;
    public Resource resourceDeleted;

    public MockResponder() {
        this.latch = new CountDownLatch(1);
    }

    public void await() throws InterruptedException {
        this.latch.await();
    }

    @Override
    public void resourceRead(Resource resource) {
        this.resourceRead = resource;
        this.latch.countDown();
    }

    @Override
    public void resourceCreated(Resource resource) {
        this.resourceCreated = resource;
        this.latch.countDown();
    }

    @Override
    public void resourceDeleted(Resource resource) {
        this.resourceDeleted = resource;
        this.latch.countDown();
    }

    @Override
    public void resourceUpdated(Resource resource) {
        this.resourceUpdated = resource;
        this.latch.countDown();
    }

    @Override
    public void createNotSupported(Resource resource) {

    }

    @Override
    public void readNotSupported(Resource resource) {

    }

    @Override
    public void updateNotSupported(Resource resource) {

    }

    @Override
    public void deleteNotSupported(Resource resource) {

    }

    @Override
    public void noSuchResource(String id) {

    }

    @Override
    public void resourceAlreadyExists(String id) {

    }

    @Override
    public void internalError(String message) {

    }

    @Override
    public void internalError(Throwable cause) {

    }

    @Override
    public void invalidRequest(String message) {

    }

    @Override
    public void invalidRequest(Throwable cause) {

    }

    @Override
    public void invalidRequest(String message, Throwable cause) {

    }
}
