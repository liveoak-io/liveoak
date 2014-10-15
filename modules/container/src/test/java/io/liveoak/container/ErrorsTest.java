/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.common.DefaultRequestContext;
import io.liveoak.common.DefaultResourceParams;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.exceptions.ForbiddenException;
import io.liveoak.spi.exceptions.NotAcceptableException;
import io.liveoak.spi.exceptions.NotAuthorizedException;
import io.liveoak.spi.exceptions.ResourceAlreadyExistsException;
import io.liveoak.spi.exceptions.ResourceException;
import io.liveoak.spi.exceptions.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class ErrorsTest {

    private LiveOakSystem system;
    private Client client;
    protected CloseableHttpClient httpClient;
    private InternalApplication application;

    @Before
    public void setUpServer() throws Exception {
        this.system = LiveOakFactory.create();

        ErrorsTestResource root = new ErrorsTestResource("errors");
        this.system.extensionInstaller().load( "errors", new ErrorsTestExtension(root) );

        // LIVEOAK-295 ... make sure system services have all started before performing programmatic application deployment
        this.system.awaitStability();
        this.application = this.system.applicationRegistry().createApplication("testApp", "Test Application");

        this.application.extend( "errors" );
        this.system.awaitStability();

        this.client = this.system.client();
    }

    @Before
    public void setUpClient() throws Exception {
        this.httpClient = HttpClientBuilder.create().build();
    }

    @After
    public void tearDownClient() throws Exception {
        this.httpClient.close();
    }

    @After
    public void tearDownServer() throws Exception {
        this.system.stop();
        System.err.flush();
    }

    @Test
    public void testSetOnPropertySink() throws Exception {
        runTests("read-properties");
    }

    @Test
    public void testThrownFromProperties() throws Exception {
        runTests("properties");
    }

    @Test
    public void testSetOnResourceSink() throws Exception {
        runTests("read-members");
    }

    @Test
    public void testThrownFromMembers() throws Exception {
        runTests("members");
    }

    @Test
    public void testThrownFromMember() throws Exception {
        runTests("member");
    }

    @Test
    public void testSetOnMemberResponder() throws Exception {
        runTests("member-responder");
    }

    @Test
    public void testThrownFromUpdate() throws Exception {
        runTests("update");
    }

    @Test
    public void testSetOnUpdateResponder() throws Exception {
        runTests("update-responder");
    }

    @Test
    public void testThrownFromCreate() throws Exception {
        runTests("create");
    }

    @Test
    public void testSetOnCreateResponder() throws Exception {
        runTests("create-responder");
    }

    @Test
    public void testThrownFromDelete() throws Exception {
        runTests("create");
    }

    @Test
    public void testSetOnDeleteResponder() throws Exception {
        runTests("create-responder");
    }

    protected void runTests(String where) throws Exception {

        String endpoint = "/testApp/errors";

        if ("member".equals(where) || "member-responder".equals(where)) {
            endpoint += "/member";
        }

        ResourceParams params = new DefaultResourceParams.Builder().build();
        ResourceState result = execute(endpoint, params, where);
        assertThat(result).isNotNull();
        assertThat(result.getProperty("status")).isEqualTo("success");

        params = new DefaultResourceParams.Builder()
                .add("action", "runtime-exception")
                .add("where", where)
                .build();
        try {
            result = execute(endpoint, params, where);
            fail("Expected an exception!");
        } catch (ResourceException e) {
            assertThat(e.state().getPropertyAsString("message").startsWith("where: " + where));
        }

        params = new DefaultResourceParams.Builder()
                .add("action", "property-exception")
                .add("where", where)
                .build();
        try {
            result = execute(endpoint, params, where);
            fail("Expected an exception!");
        } catch (NotAcceptableException e) {
            assertThat(e.state().getPropertyAsString("message").startsWith("where: " + where));
        }

        params = new DefaultResourceParams.Builder()
                .add("action", "interrupted-exception")
                .add("where", where)
                .build();
        try {
            result = execute(endpoint, params, where);
            fail("Expected an exception!");
        } catch (ResourceException e) {
            assertThat(e.state().getPropertyAsString("message").startsWith("where: " + where));
        }

        params = new DefaultResourceParams.Builder()
                .add("action", "not-acceptable-exception")
                .add("where", where)
                .build();
        try {
            result = execute(endpoint, params, where);
            fail("Expected an exception!");
        } catch (NotAcceptableException e) {
            assertThat(e.state().getPropertyAsString("message").startsWith("where: " + where));
        }

        params = new DefaultResourceParams.Builder()
                .add("action", "not-authorized-exception")
                .add("where", where)
                .build();
        try {
            result = execute(endpoint, params, where);
            fail("Expected an exception!");
        } catch (NotAuthorizedException e) {
            assertThat(e.state().getPropertyAsString("message").startsWith("where: " + where));
        }

        params = new DefaultResourceParams.Builder()
                .add("action", "forbidden-exception")
                .add("where", where)
                .build();
        try {
            result = execute(endpoint, params, where);
            fail("Expected an exception!");
        } catch (ForbiddenException e) {
            assertThat(e.state().getPropertyAsString("message").startsWith("where: " + where));
        }

        params = new DefaultResourceParams.Builder()
                .add("action", "resource-not-found-exception")
                .add("where", where)
                .build();
        try {
            result = execute(endpoint, params, where);
            fail("Expected an exception!");
        } catch (ResourceNotFoundException e) {
            // noop
        }

        params = new DefaultResourceParams.Builder()
                .add("action", "resource-already-exists-exception")
                .add("where", where)
                .build();
        try {
            result = execute(endpoint, params, where);
            fail("Expected an exception!");
        } catch (ResourceAlreadyExistsException e) {
            // noop
        }
    }

    private ResourceState execute(String endpoint, ResourceParams params, String where) throws Exception {
        if (where.startsWith("update")) {
            return client.update(requestContext(params, RequestType.UPDATE), endpoint, new DefaultResourceState("update"));
        } else if (where.startsWith("create")) {
            return client.create(requestContext(params, RequestType.CREATE), endpoint, new DefaultResourceState("create"));
        } else if (where.startsWith("delete")) {
            return client.delete(requestContext(params, RequestType.DELETE), endpoint);
        } else {
            return client.read(requestContext(params, RequestType.READ), endpoint);
        }
    }

    protected RequestContext requestContext(ResourceParams params, RequestType type) {
        return new DefaultRequestContext.Builder()
                .requestType(type)
                .resourceParams(params)
                .pagination(Pagination.NONE)
                .returnFields(DefaultReturnFields.ALL)
                .build();
    }
}
