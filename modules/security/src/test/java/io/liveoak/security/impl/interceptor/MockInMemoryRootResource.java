/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.impl.interceptor;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MockInMemoryRootResource extends MockInMemoryResource implements RootResource {

    public MockInMemoryRootResource(String id) {
        super(id);
        initMembers();
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    private void initMembers() {
        addMember("todos")
            .addProperty("somePropertyOfTodosCollection", "someValue")
            .addMember("todo1")
                .addProperty("title", "todo1")
                .addProperty("user", "john")
                .parentAsMock()
            .addMember("todo2")
                .addProperty("title", "todo2")
                .addProperty("user", "john")
                .parentAsMock()
            .addMember("todo3")
                .addProperty("title", "secret todo")
                .addProperty("user", "bob")
                .parentAsMock()
            .parentAsMock()
        .addMember("chat")
            .addMember("message1")
                .addProperty("message", "some message")
                .addProperty("author", "john")
                .parentAsMock()
            .addMember("message2")
                .addProperty("message", "secret message")
                .addProperty("author", "bob")
                .parentAsMock()
            .parentAsMock()
        .addMember("secured")
            .addProperty("title", "secret message");
    }
}
