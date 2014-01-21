/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.testtools;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.container.tenancy.InternalOrganization;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.service.ServiceContainer;
import org.junit.After;
import org.junit.Before;
import org.vertx.java.core.Vertx;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Bob McWhirter
 */
public abstract class AbstractResourceTestCase extends AbstractTestCase {

    protected LiveOakSystem system;

    protected Client client;
    protected RootResource resource;
    protected Vertx vertx;
    protected InternalOrganization organization;
    protected InternalApplication application;

    private Set<String> extensionIds = new HashSet<>();
    private Set<InternalApplicationExtension> extensions = new HashSet<>();


    public abstract void loadExtensions() throws Exception;

    protected void loadExtension(String id, Extension ext) throws Exception {
        this.system.extensionInstaller().load(id, ext);
        this.extensionIds.add(id);
    }

    protected void loadExtension(String id, Extension ext, ObjectNode config) throws Exception {
        ObjectNode fullConfig = JsonNodeFactory.instance.objectNode();
        fullConfig.put( "config", config );
        this.system.extensionInstaller().load(id, ext, fullConfig);
        this.extensionIds.add(id);
    }

    protected File applicationDirectory() {
        return null;
    }

    @Before
    public void setUpSystem() throws Exception {
        try {
            this.system = LiveOakFactory.create();
            this.organization = this.system.organizationRegistry().createOrganization("testOrg", "Test Organization");
            this.application = this.organization.createApplication("testApp", "Test Application", applicationDirectory());

            loadExtensions();

            for (String extId : this.extensionIds) {
                InternalApplicationExtension ext = this.application.extend(extId, JsonNodeFactory.instance.objectNode());
                this.extensions.add( ext );
            }

            this.client = this.system.client();
            this.vertx = this.system.vertx();

            this.system.awaitStability();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @After
    public void tearDownSystem() throws Exception {
        for (InternalApplicationExtension extension : this.extensions) {
            extension.remove();
        }

        this.system.awaitStability();
        this.system.stop();
    }

}
