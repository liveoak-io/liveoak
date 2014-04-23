/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.acl;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.testtools.resources.MockInMemoryResource;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MockAclTestStorageResource extends MockInMemoryResource implements RootResource {

    public MockAclTestStorageResource(String id) {
        super(id);
        initMembers();
    }

    private void initMembers() {
        addMember("todos");
        addMember("chat");
    }
}
