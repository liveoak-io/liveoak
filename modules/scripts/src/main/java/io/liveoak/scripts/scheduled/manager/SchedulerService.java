package io.liveoak.scripts.scheduled.manager;

import java.util.Properties;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 * TODO: this is a essentially a copy of the SchedulerService from the schedule module...
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class SchedulerService implements Service<Scheduler> {

    private static final Logger log = Logger.getLogger(SchedulerService.class);
    private String name;
    private Scheduler scheduler;

    public SchedulerService(String name) {
        this.name = name;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        try {
            StdSchedulerFactory factory = new StdSchedulerFactory(properties());
            this.scheduler = factory.getScheduler();
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext stopContext) {
        try {
            this.scheduler.shutdown();
        } catch (SchedulerException e) {
            log.error("Unable to shutdown the scheduler", e);
        }
    }

    @Override
    public Scheduler getValue() throws IllegalStateException, IllegalArgumentException {
        return scheduler;
    }

    //TODO: move this over to a configuration option in application.json?
    private Properties properties() {

        Properties props = new Properties();

        props.setProperty("org.quartz.scheduler.instanceName", this.name);
        props.setProperty("org.quartz.scheduler.rmi.export", "false");
        props.setProperty("org.quartz.scheduler.rmi.proxy", "false");
        props.setProperty("org.quartz.scheduler.wrapJobExecutionInUserTransaction", "false");

        props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.setProperty("org.quartz.threadPool.threadCount", "10");
        props.setProperty("org.quartz.threadPool.threadPriority", "5");
        props.setProperty("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");

        props.setProperty("org.quartz.jobStore.misfireThreshold", "60000");

        props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");

        return props;

    }
}
