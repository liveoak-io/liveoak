package io.liveoak.scheduler;

import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class SchedulerServices {

    private static ServiceName SCHEDULER = LiveOak.LIVEOAK.append("scheduler");

    public static ServiceName scheduler(String appId, String resourceId) {
        return SCHEDULER.append(appId, resourceId);
    }

}
