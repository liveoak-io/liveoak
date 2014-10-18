/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.ForbiddenException;
import io.liveoak.spi.exceptions.NotAcceptableException;
import io.liveoak.spi.exceptions.NotAuthorizedException;
import io.liveoak.spi.exceptions.PropertyException;
import io.liveoak.spi.exceptions.ResourceAlreadyExistsException;
import io.liveoak.spi.exceptions.ResourceNotFoundException;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class ErrorsTestResource implements RootResource, SynchronousResource {

    private Resource parent;
    private String id;

    public ErrorsTestResource(Resource parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    public ErrorsTestResource(String id) {
        this.id = id;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("status", "success");

        String where = ctx.resourceParams().value("where");
        String action = ctx.resourceParams().value("action");
        if (!"properties".equals(where) || action == null) {
            return result;
        }

        throwExceptionIfRequested(action, where);

        return result;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        String action = ctx.resourceParams().value("action");
        String where = ctx.resourceParams().value("where");

        if (action == null && where == null) {
            sink.accept("status", "success");
            sink.complete();
            return;
        }

        if (where == null) {
            throw new NotAcceptableException("Request parameter 'where' should not be null");
        }

        if (where.equals("properties")) {
            SynchronousResource.super.readProperties(ctx, sink);
            return;
        }

        try {
            if (where.equals("read-properties")) {
                throwExceptionIfRequested(action, where);
            }
            sink.accept("status", "success");
        } catch (Throwable e) {
            sink.error(e);
        } finally {
            sink.complete();
        }
    }

    @Override
    public List<Resource> members(RequestContext ctx) throws Exception {
        List<Resource> result = new LinkedList<>();

        String where = ctx.resourceParams().value("where");
        String action = ctx.resourceParams().value("action");
        if (!"members".equals(where) || action == null) {
            return result;
        }

        throwExceptionIfRequested(action, where);

        return result;
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {

        String action = ctx.resourceParams().value("action");
        String where = ctx.resourceParams().value("where");

        if (action == null && where == null) {
            sink.complete();
            return;
        }

        if (where == null) {
            throw new NotAcceptableException("Request parameter 'where' should not be null");
        }

        if (where.equals("members")) {
            SynchronousResource.super.readMembers(ctx, sink);
            return;
        }

        try {
            if (where.equals("read-members")) {
                throwExceptionIfRequested(action, where);
            }
        } catch (Throwable e) {
            sink.error(e);
        } finally {
            sink.complete();
        }
    }

    @Override
    public Resource member(RequestContext ctx, String id) throws Exception {
        Resource result = new ErrorsTestResource(this, id);

        String where = ctx.resourceParams().value("where");
        String action = ctx.resourceParams().value("action");
        if (!"member".equals(where) || action == null) {
            return result;
        }

        throwExceptionIfRequested(action, where);

        return result;
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        Resource result = new ErrorsTestResource(this, id);
        String action = ctx.resourceParams().value("action");
        String where = ctx.resourceParams().value("where");

        if (action == null && where == null) {
            responder.resourceRead(result);
            return;
        }

        if (where == null) {
            throw new NotAcceptableException("Request parameter 'where' should not be null");
        }

        if (where.equals("member")) {
            SynchronousResource.super.readMember(ctx, id, responder);
            return;
        }

        try {
            if (where.equals("member-responder")) {
                throwExceptionIfRequested(action, where);
            }

            responder.resourceRead(result);

        } catch (Throwable e) {
            responder.error(e);
        }
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

        Resource result = new ErrorsTestResource(this, "updated");
        String action = ctx.resourceParams().value("action");
        String where = ctx.resourceParams().value("where");

        if (action == null && where == null) {
            responder.resourceUpdated(this);
            return;
        }

        if (where == null) {
            throw new NotAcceptableException("Request parameter 'where' should not be null");
        }

        if (where.equals("update")) {
            throwExceptionIfRequested(action, where);
        }

        try {
            if (where.equals("update-responder")) {
                throwExceptionIfRequested(action, where);
            }

            responder.resourceUpdated(this);

        } catch (Throwable e) {
            responder.error(e);
        }
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

        Resource result = new ErrorsTestResource(this, "created");
        String action = ctx.resourceParams().value("action");
        String where = ctx.resourceParams().value("where");

        if (action == null && where == null) {
            responder.resourceCreated(result);
            return;
        }

        if (where == null) {
            throw new NotAcceptableException("Request parameter 'where' should not be null");
        }

        if (where.equals("create")) {
            throwExceptionIfRequested(action, where);
        }

        try {
            if (where.equals("create-responder")) {
                throwExceptionIfRequested(action, where);
            }

            responder.resourceCreated(result);

        } catch (Throwable e) {
            responder.error(e);
        }
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {

        String action = ctx.resourceParams().value("action");
        String where = ctx.resourceParams().value("where");

        if (action == null && where == null) {
            responder.resourceDeleted(this);
            return;
        }

        if (where == null) {
            throw new NotAcceptableException("Request parameter 'where' should not be null");
        }

        if (where.equals("delete")) {
            throwExceptionIfRequested(action, where);
        }

        try {
            if (where.equals("delete-responder")) {
                throwExceptionIfRequested(action, where);
            }

            responder.resourceDeleted(this);

        } catch (Throwable e) {
            responder.error(e);
        }
    }

    protected void throwExceptionIfRequested(String action, String where) throws PropertyException, InterruptedException, NotAcceptableException, NotAuthorizedException, ForbiddenException, ResourceNotFoundException, ResourceAlreadyExistsException {
        switch(action) {
            case "runtime-exception":
                throw new IllegalArgumentException("where: " + where + ", action: " + action);
            case "property-exception":
                throw new PropertyException("where: " + where + ", action: " + action);
            case "interrupted-exception":
                throw new InterruptedException("where: " + where + ", action: " + action);
            case "not-acceptable-exception":
                throw new NotAcceptableException(uri().toString(), "where: " + where + ", action: " + action);
            case "not-authorized-exception":
                throw new NotAuthorizedException(uri().toString(), "where: " + where + ", action: " + action);
            case "forbidden-exception":
                throw new ForbiddenException(uri().toString(), "where: " + where + ", action: " + action);
            case "resource-not-found-exception":
                throw new ResourceNotFoundException(uri().toString());
            case "resource-already-exists-exception":
                throw new ResourceAlreadyExistsException(uri().toString());
        }
    }
}
