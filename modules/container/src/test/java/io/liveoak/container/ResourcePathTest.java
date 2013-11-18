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
        ResourcePath path = new ResourcePath( "/memory/people/bob/dogs/moses" );

        assertThat( path.segments() ).hasSize( 5 );
        assertThat( path.segments().get( 0 ) ).isEqualTo( "memory" );
        assertThat( path.segments().get( 1 ) ).isEqualTo( "people" );
        assertThat( path.segments().get( 2 ) ).isEqualTo( "bob" );
        assertThat( path.segments().get( 3 ) ).isEqualTo( "dogs" );
        assertThat( path.segments().get( 4 ) ).isEqualTo( "moses" );
    }

    @Test
    public void testPathBuilding() {
        ResourcePath path = new ResourcePath();
        path.appendSegment( "memory" );
        path.appendSegment( "people" );
        path.appendSegment( "bob" );

        assertThat( path.segments() ).hasSize( 3 );
        assertThat( path.segments().get( 0 ) ).isEqualTo( "memory" );
        assertThat( path.segments().get( 1 ) ).isEqualTo( "people" );
        assertThat( path.segments().get( 2 ) ).isEqualTo( "bob" );

        path.prependSegment( "mboss" );

        assertThat( path.segments() ).hasSize( 4 );
        assertThat( path.segments().get( 0 ) ).isEqualTo( "mboss" );
        assertThat( path.segments().get( 1 ) ).isEqualTo( "memory" );
        assertThat( path.segments().get( 2 ) ).isEqualTo( "people" );
        assertThat( path.segments().get( 3 ) ).isEqualTo( "bob" );
    }

    @Test
    public void testHeadAndSubpath() {
        ResourcePath path = new ResourcePath( "/memory/people/bob" );

        assertThat( path.head() ).isEqualTo( "memory" );
        assertThat( path.subPath().head() ).isEqualTo( "people" );
        assertThat( path.subPath().subPath().head() ).isEqualTo( "bob" );
        assertThat( path.subPath().subPath().subPath().segments() ).isEmpty();
    }

}
