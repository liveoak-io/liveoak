/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.service;

import io.liveoak.security.integration.AuthzServiceConfigResource;
import io.liveoak.security.integration.AuthzServiceRootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzConfigResourceService implements Service<AuthzServiceConfigResource> {

    private String id;
    private InjectedValue<AuthzServiceRootResource> rootResourceInjector = new InjectedValue<>();
    private AuthzServiceConfigResource resource;

    public AuthzConfigResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new AuthzServiceConfigResource(
                this.id,
                this.rootResourceInjector.getValue()
        );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public AuthzServiceConfigResource getValue() {
        return this.resource;
    }

    public Injector<AuthzServiceRootResource> rootResourceInjector() {
        return this.rootResourceInjector;
    }
}
