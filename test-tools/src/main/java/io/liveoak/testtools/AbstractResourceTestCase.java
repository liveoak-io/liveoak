/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.testtools;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import org.junit.After;
import org.junit.Before;
import org.vertx.java.core.Vertx;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public abstract class AbstractResourceTestCase extends AbstractTestCase {

    protected LiveOakSystem system;

    protected Client client;
    protected RootResource resource;
    protected Vertx vertx;
    protected InternalApplication application;

    private Set<String> extensionIds = new HashSet<>();
    private Set<InternalApplicationExtension> extensions = new HashSet<>();


    public abstract void loadExtensions() throws Exception;

    protected void loadExtension(String id, Extension ext) throws Exception {
        this.system.extensionInstaller().load(id, ext);
        this.extensionIds.add(id);
        this.system.awaitStability();
    }

    protected void loadExtension(String id, Extension ext, ObjectNode extConfig) throws Exception {
        ObjectNode fullConfig = JsonNodeFactory.instance.objectNode();
        fullConfig.put( "config", extConfig );
        this.system.extensionInstaller().load(id, ext, fullConfig);
        this.extensionIds.add(id);
        this.system.awaitStability();
    }

    protected InternalApplicationExtension installResource(String extId, String resourceId, ObjectNode resourceConfig) throws Exception {
        InternalApplicationExtension appExt = this.application.extend(extId, resourceId, resourceConfig);
        this.extensions.add(appExt);
        this.system.awaitStability();
        return appExt;
    }

    protected InternalApplicationExtension installResource(String extId, String resourceId, ResourceState resourceConfig) throws Exception {
        return installResource( extId, resourceId, ConversionUtils.convert( resourceConfig ) );
    }

    protected File applicationDirectory() {
        return null;
    }

    @Before
    public void setUpSystem() throws Exception {
        try {
            this.system = LiveOakFactory.create();
            this.client = this.system.client();
            this.vertx = this.system.vertx();

            // LIVEOAK-295 ... make sure system services have all started before performing programmatic application deployment
            this.system.awaitStability();

            this.system.applicationRegistry().createApplication(ZeroExtension.APPLICATION_ID, ZeroExtension.APPLICATION_NAME);
            this.application = this.system.applicationRegistry().createApplication("testApp", "Test Application", applicationDirectory());

            this.system.awaitStability();

            // loadExtensions() relies on this.application
            loadExtensions();

            this.system.awaitStability();
            System.err.println( "stable" );
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void removeResource(InternalApplicationExtension resource) throws InterruptedException {
        this.extensions.remove(resource);
        resource.remove();

        this.system.awaitStability();
    }

    public void removeAllResources() throws InterruptedException {
        for (InternalApplicationExtension extension : this.extensions) {
            extension.remove();
        }

        this.extensions.clear();

        this.system.awaitStability();
    }

    @After
    public void tearDownSystem() throws Exception {
        removeAllResources();
        this.application.configurationFile().delete();
        this.system.stop();
    }

}
