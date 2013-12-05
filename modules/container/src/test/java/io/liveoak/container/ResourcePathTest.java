/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.spi.ResourcePath;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ResourcePathTest {

    @Test
    public void testPathParsing() {
        ResourcePath path = new ResourcePath("/memory/people/bob/dogs/moses");

        assertThat(path.segments()).hasSize(5);
        assertThat(path.segments().get(0).name()).isEqualTo("memory");
        assertThat(path.segments().get(1).name()).isEqualTo("people");
        assertThat(path.segments().get(2).name()).isEqualTo("bob");
        assertThat(path.segments().get(3).name()).isEqualTo("dogs");
        assertThat(path.segments().get(4).name()).isEqualTo("moses");
    }

    @Test
    public void testPathBuilding() {
        ResourcePath path = new ResourcePath();
        path.appendSegment("memory");
        path.appendSegment("people");
        path.appendSegment("bob");

        assertThat(path.segments()).hasSize(3);
        assertThat(path.segments().get(0).name()).isEqualTo("memory");
        assertThat(path.segments().get(1).name()).isEqualTo("people");
        assertThat(path.segments().get(2).name()).isEqualTo("bob");

        path.prependSegment("mboss");

        assertThat(path.segments()).hasSize(4);
        assertThat(path.segments().get(0).name()).isEqualTo("mboss");
        assertThat(path.segments().get(1).name()).isEqualTo("memory");
        assertThat(path.segments().get(2).name()).isEqualTo("people");
        assertThat(path.segments().get(3).name()).isEqualTo("bob");
    }

    @Test
    public void testHeadAndSubpath() {
        ResourcePath path = new ResourcePath("/memory/people/bob");

        assertThat(path.head().name()).isEqualTo("memory");
        assertThat(path.subPath().head().name()).isEqualTo("people");
        assertThat(path.subPath().subPath().head().name()).isEqualTo("bob");
        assertThat(path.subPath().subPath().subPath().segments()).isEmpty();
    }

}
