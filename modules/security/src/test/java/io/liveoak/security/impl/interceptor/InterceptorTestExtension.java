/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.impl.interceptor;

import io.liveoak.interceptor.service.InterceptorRegistrationHelper;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InterceptorTestExtension implements Extension {

    private final MockAuthInterceptor interceptor;

    public InterceptorTestExtension(MockAuthInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        ServiceTarget target = context.target();
        InterceptorRegistrationHelper.installInterceptor(target, "mock-auth", interceptor);
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
