/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.logging.fs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import io.liveoak.filesystem.DirectoryResource;
import io.liveoak.filesystem.FileResource;
import io.liveoak.logging.config.LogDirConfig;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.VertxResource;
import io.liveoak.spi.resource.async.ResourceSink;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class LogDirectoryResource extends DirectoryResource {

    private final FilenameFilter filter;

    public LogDirectoryResource(VertxResource parent, LogDirConfig config) {
        this(parent, new File(config.path()), new PatternFileNameFilter(config.filter()), true);
    }

    public LogDirectoryResource(VertxResource parent, File file, FilenameFilter filter, boolean writable) {
        super(parent, file, writable);
        this.filter = filter;
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        super.readMembers(ctx, sink);
    }

    @Override
    protected DirectoryResource createDirectoryResource(File path) {
        if (filter.accept(path, path.getName())) {
            return new LogDirectoryResource(this, path, filter, writable());
        }

        return null;
    }

    @Override
    protected FileResource createFileResource(File file) {
        if (filter.accept(file.getParentFile(), file.getName())) {
            return new LogFileResource(this, file);
        }

        return null;
    }

    private static class PatternFileNameFilter implements FilenameFilter {
        private final Pattern pattern;

        private PatternFileNameFilter(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        public boolean accept(File dir, String name) {
            return pattern.matcher(name).matches();
        }

        private Pattern pattern() {
            return pattern;
        }
    }
}
