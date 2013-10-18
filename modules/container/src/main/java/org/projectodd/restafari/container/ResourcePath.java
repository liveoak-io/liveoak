package org.projectodd.restafari.container;

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

    public ResourcePath(String... segments) {
        this();
        for (int i = 0; i < segments.length; ++i) {
            this.segments.add(segments[i]);
        }
    }

    public ResourcePath(String uri) {
        this();
        StringTokenizer tokens = new StringTokenizer(uri, "/");

        while (tokens.hasMoreTokens()) {
            this.segments.add(tokens.nextToken());
        }
    }

    ResourcePath(List<String> segments) {
        this.segments = segments;
    }

    public void appendSegment(String segment) {
        this.segments.add(segment);
    }

    public void prependSegment(String segment) {
        this.segments.add(0, segment);
    }

    public String head() {
        if (this.segments.size() > 0) {
            return this.segments.get(0);
        }
        return null;
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

    public List<String> segments() {
        return this.segments;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.segments.forEach((s) -> {
            builder.append( "/" ).append( s);
        });
        return builder.toString();
    }

    private List<String> segments;

}
