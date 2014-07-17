/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.acl.service;

import io.liveoak.security.policy.acl.impl.AclPolicy;
import io.liveoak.security.policy.acl.integration.AclPolicyConfigResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyConfigResourceService implements Service<AclPolicyConfigResource> {

    public AclPolicyConfigResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new AclPolicyConfigResource(this.id, this.policyInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public AclPolicyConfigResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<AclPolicy> policyInjector() {
        return this.policyInjector;
    }

    private String id;
    private InjectedValue<AclPolicy> policyInjector = new InjectedValue<>();
    private AclPolicyConfigResource resource;
}
