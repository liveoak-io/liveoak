/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.bootstrap;

import java.io.File;

import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;

/**
 * Bootstrapping <code>main()</code> method.
 *
 * @author Bob McWhirter
 */
public class Bootstrap {
    public static void main(String... args) throws Exception {
        System.err.println("LiveOak BaaS");

        File configDir = null;

        if ( args.length == 1 ) {
            configDir = new File( args[0] );
        }

        LiveOakSystem system = LiveOakFactory.create( configDir );
    }
}
