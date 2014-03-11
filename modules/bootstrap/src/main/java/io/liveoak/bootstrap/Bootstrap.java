/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.bootstrap;

import io.liveoak.autosetup.LiveOakAutoSetup;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.container.tenancy.InternalApplication;
import org.jboss.logging.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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

        String bindAddress = "localhost";

        List<String> argsList = new LinkedList<>(Arrays.asList(args));
        ListIterator<String> itr = argsList.listIterator();
        while (itr.hasNext()) {
            String a = itr.next();
            if (a.equals("-b")) {
                itr.remove();
                bindAddress = itr.next();
                itr.remove();

                log.info("bind address: " + bindAddress);
            } else if (a.startsWith("-D")) {
                String[] prop = a.substring(2).split("=");
                System.setProperty(prop[0], prop[1]);
                itr.remove();
            }
        }

        itr = argsList.listIterator();

        if (itr.hasNext()) {
            liveOakDir = new File(itr.next());
            configDir = new File(liveOakDir, "conf");
            appsDir = new File(liveOakDir, "apps");
        }

        log.info("LiveOak BaaS auto-setup");
        LiveOakAutoSetup.run(liveOakDir, configDir);

        log.info("Booting up LiveOak BaaS");
        long start = System.currentTimeMillis();

        LiveOakSystem system = LiveOakFactory.create(configDir, appsDir, null, bindAddress);

        log.infof("LiveOak booted in %d ms", (System.currentTimeMillis() - start));

        while (itr.hasNext()) {
            String app = itr.next();
            File appDir = new File(app).getCanonicalFile();
            InternalApplication deployedApp = system.applicationRegistry().createApplication(appDir.getName(), appDir.getName(), appDir);
            system.awaitStability();
        }
    }
}
