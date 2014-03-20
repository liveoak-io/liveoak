/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.git;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.git.extension.GitExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class GitResourceTest extends AbstractResourceTestCase {

    @Override
    protected File applicationDirectory() {
        return new File( this.projectRoot, "target/test-app" );
    }

    @Override
    public void loadExtensions() throws Exception {
        new File( applicationDirectory(), "git" ).mkdirs();
        loadExtension( "git", new GitExtension() );
        installResource( "git", "git", JsonNodeFactory.instance.objectNode() );
    }

    @Test
    public void testRoot() throws Exception {
        ResourceState result = this.client.read(new RequestContext.Builder().build(), "/testApp/git");

        assertThat(result).isNotNull();
    }

}
