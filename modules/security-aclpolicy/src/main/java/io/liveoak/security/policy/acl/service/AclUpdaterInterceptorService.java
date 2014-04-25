/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.acl.service;

import io.liveoak.security.policy.acl.interceptor.AclUpdaterInterceptor;
import io.liveoak.spi.client.Client;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclUpdaterInterceptorService implements Service<AclUpdaterInterceptor> {

    private InjectedValue<Client> clientInjector = new InjectedValue<>();
    private AclUpdaterInterceptor aclInterceptor;

    @Override
    public void start(StartContext context) throws StartException {
        aclInterceptor = new AclUpdaterInterceptor(clientInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        aclInterceptor = null;
    }

    @Override
    public AclUpdaterInterceptor getValue() throws IllegalStateException, IllegalArgumentException {
        return aclInterceptor;
    }

    public Injector<Client> clientInjector() {
        return clientInjector;
    }

}
