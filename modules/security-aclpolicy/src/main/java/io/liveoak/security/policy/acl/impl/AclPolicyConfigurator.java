/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.acl.impl;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyConfigurator {

    private static final Logger log = Logger.getLogger(AclPolicyConfigurator.class);

    public void configure(AclPolicy policy, AclPolicyConfig aclPolicyConfig) {
        policy.setPolicyConfig(aclPolicyConfig);
        log.info("AclPolicy configuration successfully updated");
    }
}
