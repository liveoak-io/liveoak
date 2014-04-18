/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.drools.service;

import io.liveoak.security.policy.drools.impl.DroolsPolicy;
import io.liveoak.security.policy.drools.integration.DroolsPolicyConfigResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DroolsPolicyConfigResourceService implements Service<DroolsPolicyConfigResource> {

    public DroolsPolicyConfigResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new DroolsPolicyConfigResource( this.id, this.policyInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public DroolsPolicyConfigResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<DroolsPolicy> policyInjector() {
        return this.policyInjector;
    }

    private String id;
    private InjectedValue<DroolsPolicy> policyInjector = new InjectedValue<>();
    private DroolsPolicyConfigResource resource;
}
