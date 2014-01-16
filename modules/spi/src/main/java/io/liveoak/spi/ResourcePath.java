/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Bob McWhirter
 */
public class ResourcePath {

    public ResourcePath() {
        this.segments = new ArrayList<>();
    }

    public ResourcePath(String... segments) {
        this();
        for (int i = 0; i < segments.length; ++i) {
            //this.segments.add(segments[i]);
            this.segments.add( new Segment( segments[i]) );
        }
    }

    public ResourcePath(String uri) {
        this();
        StringTokenizer tokens = new StringTokenizer(uri, "/");

        while (tokens.hasMoreTokens()) {
            this.segments.add( new Segment( tokens.nextToken()) );
        }
    }

    public ResourcePath(ResourcePath path) {
        this.segments = new ArrayList(path.segments);
    }

    public ResourcePath(List<Segment> segments) {
        this.segments = segments;
    }

    public void appendSegment(String segment) {
        this.segments.add( new Segment( segment) );
    }

    public void prependSegment(String segment) {
        this.segments.add(0, new Segment( segment) );
    }

    public Segment head() {
        if (this.segments.size() > 0) {
            return this.segments.get(0);
        }
        return null;
    }

    public Segment tail() {
        if (segments.size() > 0) {
            return segments.get(segments.size()-1);
        }
        return null;
    }

    public ResourcePath parent() {
        if (this.segments.isEmpty()) {
            return new ResourcePath();
        }
        return new ResourcePath(segments.subList(0, segments.size() - 1));
    }

    public ResourcePath subPath() {
        if (this.segments.isEmpty()) {
            return new ResourcePath();
        }
        return new ResourcePath(segments.subList(1, segments.size()));
    }

    public boolean isEmpty() {
        return this.segments.isEmpty();
    }

    public List<Segment> segments() {
        return this.segments;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.segments.forEach((s) -> {
            builder.append("/").append(s);
        });
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(this.getClass().equals(obj.getClass()))) {
            return false;
        }

        ResourcePath that = (ResourcePath) obj;
        return segments.equals(that.segments);
    }

    @Override
    public int hashCode() {
        return segments.hashCode();
    }

    public static class Segment {

        public Segment(String value) {
            int semiLoc = value.indexOf(';');
            if ( semiLoc < 0 ) {
                this.name = value;
                this.matrixParameters = new MatrixParameters();
            } else {
                this.name = value.substring(0, semiLoc );
                this.matrixParameters = new MatrixParameters( value.substring( semiLoc+1 ) );
            }
        }

        public String name() {
            return this.name;
        }

        public MatrixParameters matrixParameters() {
            return this.matrixParameters;
        }

        @Override
        public boolean equals(Object obj) {
            if ( obj instanceof Segment ) {
                return this.name.equals( ((Segment) obj).name() );
            }
            return false;
        }

        public String toString() {
            return this.name;
        }

        private String name;
        private MatrixParameters matrixParameters = new MatrixParameters();
    }

    public static class MatrixParameters extends HashMap<String,String> {

        public MatrixParameters() {

        }

        public MatrixParameters(String params) {

            StringTokenizer tokens = new StringTokenizer(params, ";");

            while (tokens.hasMoreTokens()) {
                String one = tokens.nextToken();
                int equalsLoc = one.indexOf('=');

                if ( equalsLoc < 0 ) {
                    put( one, "true" );
                } else {
                    put( one.substring( 0, equalsLoc ), one.substring( equalsLoc + 1 ) );
                }
            }
        }
    }

    private List<Segment> segments;

}
