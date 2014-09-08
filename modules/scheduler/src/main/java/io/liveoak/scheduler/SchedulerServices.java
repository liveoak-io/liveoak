package io.liveoak.scheduler;

import io.liveoak.spi.Services;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class SchedulerServices {

    private static ServiceName SCHEDULER = Services.LIVEOAK.append("scheduler");

    public static ServiceName scheduler(String appId, String resourceId) {
        return SCHEDULER.append(appId, resourceId);
    }

}
