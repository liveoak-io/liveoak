/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Bob McWhirter
 */
public class ResourcePath {

    public ResourcePath() {
        this.segments = new ArrayList<>();
    }

    public ResourcePath( String... segments ) {
        this();
        for ( int i = 0; i < segments.length; ++i ) {
            this.segments.add( segments[i] );
        }
    }

    public ResourcePath( String uri ) {
        this();
        StringTokenizer tokens = new StringTokenizer( uri, "/" );

        while ( tokens.hasMoreTokens() ) {
            this.segments.add( tokens.nextToken() );
        }
    }

    ResourcePath( List<String> segments ) {
        this.segments = segments;
    }

    public void appendSegment( String segment ) {
        this.segments.add( segment );
    }

    public void prependSegment( String segment ) {
        this.segments.add( 0, segment );
    }

    public String head() {
        if ( this.segments.size() > 0 ) {
            return this.segments.get( 0 );
        }
        return null;
    }

    public ResourcePath subPath() {
        if ( this.segments.isEmpty() ) {
            return new ResourcePath();
        }
        return new ResourcePath( segments.subList( 1, segments.size() ) );
    }

    public boolean isEmpty() {
        return this.segments.isEmpty();
    }

    public List<String> segments() {
        return this.segments;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.segments.forEach( ( s ) -> {
            builder.append( "/" ).append( s );
        } );
        return builder.toString();
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj == null || !( this.getClass().equals( obj.getClass() ) ) ) {
            return false;
        }

        ResourcePath that = ( ResourcePath ) obj;
        return segments.equals( that.segments );
    }

    @Override
    public int hashCode() {
        return segments.hashCode();
    }

    private List<String> segments;

}
