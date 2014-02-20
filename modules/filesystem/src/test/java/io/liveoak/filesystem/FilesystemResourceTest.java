/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.filesystem.extension.FilesystemExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class FilesystemResourceTest extends AbstractResourceTestCase {

    @Override
    protected File applicationDirectory() {
        return this.projectRoot;
    }

    @Override
    public void loadExtensions() throws Exception {
        loadExtension("fs", new FilesystemExtension());
        installResource( "fs", "files", JsonNodeFactory.instance.objectNode() );
    }

    @Test
    public void testRoot() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), "/testApp/files");
        assertThat(result).isNotNull();
        assertThat(result.members()).hasSize(1);
        ResourceState file = result.members().get(0);
        assertThat(file.id()).isEqualTo("test-file1.txt");
    }

    @Test
    public void testChild() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), "/testApp/files/test-file1.txt");
        assertThat(result).isNotNull();
    }

}
