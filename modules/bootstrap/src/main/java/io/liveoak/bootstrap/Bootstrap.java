/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.bootstrap;

import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.container.tenancy.InternalApplication;
import org.jboss.logging.Logger;

import java.io.File;

/**
 * Bootstrapping <code>main()</code> method.
 *
 * @author Bob McWhirter
 */
public class Bootstrap {
    private static final Logger log = Logger.getLogger("io.liveoak.bootstrap");

    public static void main(String... args) throws Exception {
        File liveOakDir = null;
        File configDir = null;
        File appsDir = null;

        if (args.length >= 1) {
            liveOakDir = new File(args[0]);
            configDir = new File(liveOakDir, "conf");
            appsDir = new File(liveOakDir, "apps");
        }

        log.info("Booting up LiveOak BaaS");
        long start = System.currentTimeMillis();

        LiveOakSystem system = LiveOakFactory.create(configDir, appsDir);

        log.infof("LiveOak booted in %d ms", (System.currentTimeMillis() - start));

        if ( args.length == 2 ) {
            File appDir = new File( args[1] ).getCanonicalFile();
            InternalApplication deployedApp = system.applicationRegistry().createApplication(appDir.getName(), appDir.getName(), appDir);
            system.awaitStability();
        }
    }
}
