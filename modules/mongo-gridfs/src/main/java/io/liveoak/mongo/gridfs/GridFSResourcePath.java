/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GridFSResourcePath extends ResourcePath {

    public GridFSResourcePath(ResourcePath path) {
        super(path);
    }

    public GridFSResourcePath(String... segments) {
        super(segments);
    }

    public static GridFSResourcePath fromContext(RequestContext ctx) {
        return new GridFSResourcePath(ctx.resourcePath());
    }

    public static GridFSResourcePath fromUri(URI uri) {
        return new GridFSResourcePath(uri.toString().substring(1).split("/"));
    }

    public boolean isSingle() {
        return segments().size() == 1;
    }

    @Override
    public GridFSResourcePath subPath() {
        return new GridFSResourcePath(super.subPath());
    }

    public GridFSResourcePath append(String segment) {
        GridFSResourcePath copy = new GridFSResourcePath(this);
        copy.appendSegment(segment);
        return copy;
    }

    public GridFSResourcePath top(int count) {
        List<Segment> segs = segments();
        if (segs == null || segs.size() == 0) {
            segs = null;
        } else {
            segs = segs.subList(0, count);
        }
        return new GridFSResourcePath(new ResourcePath(segs));
    }

    public boolean equals(Object obj) {
        if (obj instanceof ResourcePath == false) {
            return false;
        }
        return segments().equals(((ResourcePath)obj).segments());
    }

    public int hashCode() {
        return segments().hashCode();
    }
}
