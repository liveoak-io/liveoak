package io.liveoak.security.policy.uri.complex;

import io.liveoak.container.auth.SimpleLogger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LoggerHelper {

    private static final SimpleLogger log = new SimpleLogger("DROOLS");

    public static SimpleLogger logger() {
        return log;
    }
}
