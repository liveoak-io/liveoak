/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.resource;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;

/**
 * @author Bob McWhirter
 */
public class ContainerResource implements  RootResource {


    public ContainerResource(String id) {
        this.id = id;
        this.propertiesResource = new PropertiesResource( this );
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        sink.accept( this.propertiesResource );
        try {
            sink.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        if ( id.equals( this.propertiesResource.id() ) ) {
            responder.resourceRead( this.propertiesResource );
        } else {
            responder.noSuchResource( id );
        }
    }

    private String id;

    private PropertiesResource propertiesResource;
}
