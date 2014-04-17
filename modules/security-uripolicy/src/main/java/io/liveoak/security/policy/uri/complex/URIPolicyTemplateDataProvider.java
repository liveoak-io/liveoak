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

    private final URIPolicyRule uriPolicyRule;
    private boolean processed = false;

    public URIPolicyTemplateDataProvider(URIPolicyRule uriPolicyRule) {
        this.uriPolicyRule = uriPolicyRule;
    }

    @Override
    public boolean hasNext() {
        return !processed;
    }

    @Override
    public String[] next() {
        processed = true;

        return new String[]{
                String.valueOf(uriPolicyRule.getId()),
                String.valueOf(uriPolicyRule.getPriority()),
                String.valueOf(uriPolicyRule.getPriority() - 1),
                uriPolicyRule.getUriPattern(),
                uriPolicyRule.getQueryParamsCondition(),
                uriPolicyRule.getRequestType(),
                uriPolicyRule.getAllowedRoles(),
                uriPolicyRule.getDeniedRoles(),
                uriPolicyRule.getAllowedUsers(),
                uriPolicyRule.getDeniedUsers()
        };
    }
}
