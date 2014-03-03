/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.autosetup;

import java.io.File;
import io.liveoak.mongo.launcher.MongoLauncherAutoSetup;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class LiveOakAutoSetup {

    public static void run(File liveoakDir, File confDir) throws Exception {

        MongoLauncherAutoSetup.setup(liveoakDir, confDir);
    }
}
