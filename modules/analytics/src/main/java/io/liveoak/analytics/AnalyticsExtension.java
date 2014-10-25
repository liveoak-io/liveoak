/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.analytics;

import io.liveoak.container.analytics.AnalyticsService;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceName;

public class AnalyticsExtension implements Extension {

    private ServiceName analyticsServiceName;

    @Override
    public void extend(SystemExtensionContext context) throws Exception {

        analyticsServiceName = Services.instanceResource(context.moduleId(), context.id());
        // install AnalyticsService that handles Analytics singleton
        AnalyticsService analyticsService = new AnalyticsService();
        context.target().addService(analyticsServiceName, analyticsService)
                .install();

        // install AnalyticsExtensionConfigService which provides extension config root resource (rest endpoint) via getValue()
        ServiceName configServiceName = Services.systemResource(context.moduleId(), context.id());
        AnalyticsExtensionConfigService configService = new AnalyticsExtensionConfigService(context.id());
        context.target().addService(configServiceName, configService)
                .addDependency(analyticsServiceName, AnalyticsService.class, configService.analyticsServiceInjector())
                .install();

        context.mountPrivate(configServiceName);
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        // install AnalyticsExtensionConfigService which provides extension config root resource (rest endpoint) via getValue()
        ServiceName configServiceName = Services.adminResource(context.application().id(), context.resourceId());
        AnalyticsApplicationConfigService configService = new AnalyticsApplicationConfigService(context.resourceId());
        context.target().addService(configServiceName, configService)
                .addDependency(analyticsServiceName, AnalyticsService.class, configService.analyticsServiceInjector())
                .install();

        context.mountPrivate();


        ServiceName restServiceName = Services.resource(context.application().id(), context.resourceId());
        AnalyticsRootService rootResource = new AnalyticsRootService(context.resourceId());
        context.target().addService(restServiceName, rootResource)
                .addDependency(analyticsServiceName, AnalyticsService.class, rootResource.analyticsServiceInjector())
                .install();

        context.mountPublic();
    }

    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
/*
    public void instance(String id, SystemExtensionContext context) throws Exception {
        ServiceName instanceName = Services.instanceResource(context.moduleId(), id);
        AnalyticsService analyticsService = new AnalyticsService();
        context.target().addService(instanceName, analyticsService)
                .addDependency(SYSTEM_MONGO_DATASTORE_CONFIG_SERVICE, MongoDatastoresRegistry.class, analyticsService.mongoDatastoreInjector)
                .addInjection(analyticsService.idInjector, id)
                .install();
        context.mountInstance(instanceName);
    }
*/
}
