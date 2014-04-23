/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.impl.interceptor;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.testtools.resources.MockInMemoryResource;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MockInMemoryRootResource extends MockInMemoryResource implements RootResource {

    public MockInMemoryRootResource(String id) {
        super(id);
        initMembers();
    }

    private void initMembers() {
        addMember("todos")
            .putProperty("somePropertyOfTodosCollection", "someValue")
            .addMember("todo1")
                .putProperty("title", "todo1")
                .putProperty("user", "john")
                .parentAsMock()
            .addMember("todo2")
                .putProperty("title", "todo2")
                .putProperty("user", "john")
                .parentAsMock()
            .addMember("todo3")
                .putProperty("title", "secret todo")
                .putProperty("user", "bob")
                .parentAsMock()
            .parentAsMock()
        .addMember("chat")
            .addMember("message1")
                .putProperty("message", "some message")
                .putProperty("author", "john")
                .parentAsMock()
            .addMember("message2")
                .putProperty("message", "secret message")
                .putProperty("author", "bob")
                .parentAsMock()
            .parentAsMock()
        .addMember("secured")
            .putProperty("title", "secret message");
    }
}
