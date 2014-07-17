/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.launcher.extension;

import io.liveoak.mongo.launcher.config.MongoLauncherConfigResource;
import io.liveoak.mongo.launcher.service.MongoLauncherService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MongoLauncherExtension implements Extension {

    public static final ServiceName MONGO_LAUNCHER_NAME = LiveOak.LIVEOAK.append("mongo-launcher");
    public static final ServiceName MONGO_LAUNCHER_CONFIG_NAME = MONGO_LAUNCHER_NAME.append("config");

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        // mount config resource so we can get access to extension configuration
        MongoLauncherConfigResource configResource = new MongoLauncherConfigResource(context.id());
        ValueService<RootResource> configService = new ValueService(new ImmediateValue(configResource));
        context.target().addService(MONGO_LAUNCHER_CONFIG_NAME, configService).install();
        context.mountPrivate(MONGO_LAUNCHER_CONFIG_NAME);

        // start launcher service which uses extension configuration via configResource
        // make sure to wait for configResource to get mounted in order to make sure
        // it was updated with extension configuration
        MongoLauncherService launcherService = new MongoLauncherService(context.id());
        context.target().addService(MONGO_LAUNCHER_NAME, launcherService)
                .addDependency(MONGO_LAUNCHER_CONFIG_NAME.append("mount"))
                .addInjection(launcherService.configResourceInjector(), configResource)
                .install();
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

    }


    public void unextend(ApplicationExtensionContext context) throws Exception {

    }

}
