package io.liveoak.scripts.objects;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScheduledContext {

    private String id;
    private JobExecutionContext context;

    private Trigger trigger = null;
    private Long executionCount = null;

    ScheduledContext(String id, JobExecutionContext context) {
        this.id = id;
        this.context = context;
    }

    public Long getExecutionCount() throws Exception {
        if (executionCount == null) {
            JobDetail jobDetail = context.getJobDetail();
            if (jobDetail != null && jobDetail.getJobDataMap() != null) {
                Long executions = (Long) jobDetail.getJobDataMap().get("executions");
                if (executions != null) {
                    executionCount = executions;
                } else {
                    executionCount = 0L;
                }
            } else {
                executionCount = 0L;
            }
        }
        return executionCount;
    }

    public String getId() {
        return context.getFireInstanceId();
    }

    public Date getFireTime() {
        return context.getFireTime();
    }

    public Date getScheduledFireTime() {
        return context.getScheduledFireTime();
    }

    public Trigger getTrigger() throws Exception {
        if (trigger == null) {
            trigger = new TriggerBuilder(id, context.getScheduler()).build();
        }
        return trigger;
    }
}
