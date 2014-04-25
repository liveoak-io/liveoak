/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.acl.service;

import com.mongodb.DBCollection;
import io.liveoak.mongo.internal.InternalStorage;
import io.liveoak.security.policy.acl.impl.AclPolicy;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyService implements Service<AclPolicy> {

    private AclPolicy aclPolicy;
    private final InjectedValue<InternalStorage> mongoStorageInjector = new InjectedValue<>();

    @Override
    public void start(StartContext context) throws StartException {
        InternalStorage mongoStorage = this.mongoStorageInjector.getValue();
        DBCollection aclCollection = mongoStorage.getCollection();
        this.aclPolicy = new AclPolicy( aclCollection );
    }

    @Override
    public void stop(StopContext context) {
        this.aclPolicy = null;
    }

    @Override
    public AclPolicy getValue() throws IllegalStateException, IllegalArgumentException {
        return this.aclPolicy;
    }

    public Injector<InternalStorage> mongoStorageInjector() {
        return this.mongoStorageInjector;
    }
}
