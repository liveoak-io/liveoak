/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri.complex;

import org.drools.template.DataProvider;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Drools DataProvider for provide data about URI policies configured by user. Data are used to compile Drools template into
 * real rules for Drools engine
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyTemplateDataProvider implements DataProvider {

    private final URIPolicyEntry uriPolicyEntry;
    private boolean processed = false;

    private static AtomicInteger counter = new AtomicInteger(0);

    public URIPolicyTemplateDataProvider(URIPolicyEntry uriPolicyEntry) {
        this.uriPolicyEntry = uriPolicyEntry;
    }

    @Override
    public boolean hasNext() {
        return !processed;
    }

    @Override
    public String[] next() {
        processed = true;

        return new String[]{
                String.valueOf(counter.incrementAndGet()),
                String.valueOf(uriPolicyEntry.getPriority()),
                String.valueOf(uriPolicyEntry.getPriority() - 1),
                uriPolicyEntry.getUriPattern(),
                uriPolicyEntry.getQueryParamsCondition(),
                uriPolicyEntry.getRequestType(),
                uriPolicyEntry.getAllowedRealmRoles(),
                uriPolicyEntry.getDeniedRealmRoles(),
                uriPolicyEntry.getAllowedApplicationRoles(),
                uriPolicyEntry.getDeniedApplicationRoles(),
                uriPolicyEntry.getAllowedUsers(),
                uriPolicyEntry.getDeniedUsers()
        };
    }
}
