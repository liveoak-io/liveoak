package io.liveoak.security.policy.drools.impl;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LoggerHelper {

    private static final Logger log = Logger.getLogger("DROOLS");

    public static Logger logger() {
        return log;
    }
}
