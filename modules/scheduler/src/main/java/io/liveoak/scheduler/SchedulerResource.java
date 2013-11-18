package io.liveoak.scheduler;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;
import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class SchedulerResource implements RootResource {

    public SchedulerResource() {

    }

    public SchedulerResource( String id ) {
        this.id = id;
    }

    @Override
    public String id() {
        return this.id;
    }

    public Scheduler scheduler() {
        return this.scheduler;
    }

    @Override
    public void initialize( ResourceContext context ) throws InitializationException {
        if ( this.id == null ) {
            this.id = context.config().get( "id", null );
        }

        if ( this.id == null ) {
            throw new InitializationException( "id cannot be null" );
        }

        try {
            StdSchedulerFactory factory = new StdSchedulerFactory();
            this.scheduler = factory.getScheduler();
            this.scheduler.start();
        } catch ( SchedulerException e ) {
            throw new InitializationException( e );
        }
    }

    @Override
    public void destroy() {
        try {
            this.scheduler.shutdown();
        } catch ( SchedulerException e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void readProperties( RequestContext ctx, PropertySink sink ) {
        try {
            if ( this.scheduler.isStarted() ) {
                sink.accept( "status", "started" );
            } else {
                sink.accept( "stats", "stopped" );
            }

            sink.accept( "name", this.scheduler.getSchedulerName() );
            sink.accept( "instance-id", this.scheduler.getSchedulerInstanceId() );
            SchedulerMetaData metaData = this.scheduler.getMetaData();

            sink.accept( "running-since", metaData.getRunningSince() );
        } catch ( SchedulerException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        sink.close();
    }

    @Override
    public void readMember( RequestContext ctx, String id, Responder responder ) {
        try {
            Trigger trigger = this.scheduler.getTrigger( TriggerKey.triggerKey( id ) );
            responder.resourceRead( new TriggerResource( this, trigger ) );
        } catch ( SchedulerException e ) {
            responder.noSuchResource( id );
        }
    }

    @Override
    public void createMember( RequestContext ctx, ResourceState state, Responder responder ) {

        try {
            String id = UUID.randomUUID().toString();
            JobKey jobKey = JobKey.jobKey( id );

            CronTriggerImpl trigger = new CronTriggerImpl();
            trigger.setCronExpression( ( String ) state.getProperty( "cron" ) );
            trigger.setName( id );

            JobDetailImpl jobDetail = new JobDetailImpl();
            jobDetail.setKey( jobKey );
            jobDetail.setJobClass( LiveOakJob.class );

            this.scheduler.scheduleJob( jobDetail, trigger );

            responder.resourceCreated( new TriggerResource( this, trigger ) );

        } catch ( ParseException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch ( SchedulerException e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private String id;
    private Scheduler scheduler;
}
