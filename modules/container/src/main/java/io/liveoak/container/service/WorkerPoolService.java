package io.liveoak.container.service;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class WorkerPoolService implements Service<Executor> {

    @Override
    public void start(StartContext context) throws StartException {
        this.workerPool = Executors.newCachedThreadPool();
    }

    @Override
    public void stop(StopContext context) {
        this.workerPool.shutdown();
        this.workerPool = null;
    }

    @Override
    public Executor getValue() throws IllegalStateException, IllegalArgumentException {
        return this.workerPool;
    }

    private ExecutorService workerPool;
}
