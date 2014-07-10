package io.liveoak.scheduler.service;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Properties;

/**
 * @author Bob McWhirter
 */
public class SchedulerService implements Service<Scheduler> {

    private static final Logger log = Logger.getLogger(SchedulerService.class);

    public SchedulerService(String name) {
        this.name = name;
    }

    @Override
    public void start(StartContext context) throws StartException {
        try {
            StdSchedulerFactory factory = new StdSchedulerFactory( properties() );
            this.scheduler = factory.getScheduler();
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
        try {
            this.scheduler.shutdown();
        } catch (SchedulerException e) {
            log.error("Unable to shutdown the scheduler", e);
        }
    }

    @Override
    public Scheduler getValue() throws IllegalStateException, IllegalArgumentException {
        return this.scheduler;
    }

    private Properties properties() {

        Properties props = new Properties();

        props.setProperty( "org.quartz.scheduler.instanceName", this.name );
        props.setProperty( "org.quartz.scheduler.rmi.export", "false" );
        props.setProperty( "org.quartz.scheduler.rmi.proxy", "false" );
        props.setProperty( "org.quartz.scheduler.wrapJobExecutionInUserTransaction", "false" );

        props.setProperty( "org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool" );
        props.setProperty( "org.quartz.threadPool.threadCount", "10" );
        props.setProperty( "org.quartz.threadPool.threadPriority", "5" );
        props.setProperty( "org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true" );

        props.setProperty( "org.quartz.jobStore.misfireThreshold", "60000" );

        props.setProperty( "org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore" );

        return props;

    }

    private String name;

    private Scheduler scheduler;
}
