/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.bootstrap;

import java.io.File;

import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import org.jboss.logging.Logger;

/**
 * Bootstrapping <code>main()</code> method.
 *
 * @author Bob McWhirter
 */
public class Bootstrap {
    private static final Logger log = Logger.getLogger("io.liveoak.bootstrap");

    public static void main(String... args) throws Exception {
        File configDir = null;

        if ( args.length == 1 ) {
            configDir = new File( args[0] );
        }

        log.info("Booting up LiveOak BaaS");
        long start = System.currentTimeMillis();

        LiveOakFactory.create(configDir);

        log.infof("LiveOak booted in %d ms", (System.currentTimeMillis() - start));
    }
}
