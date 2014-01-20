/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem.aggregating;

import io.liveoak.filesystem.FileResource;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.BinaryContentSink;
import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import org.jboss.logging.Logger;
import org.vertx.java.core.buffer.Buffer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class AggregatingResource implements BinaryResource {

    public AggregatingResource(Resource parent, String id, FileResource manifest) {
        this.parent = parent;
        this.id = id;
        this.manifest = manifest;
    }

    @Override
    public MediaType mediaType() {
        int dotLoc = this.id.lastIndexOf('.');
        String extension = this.id.substring(dotLoc + 1);
        return MediaType.lookup(extension);
    }

    @Override
    public long contentLength() {
        List<File> files = getFilesList();
        long total = 0;
        for (File file: files) {
            total += file.length();
        }
        return total;
    }


    private List<File> getFilesList() {
        if (filesList == null) {
            List<File> list = new LinkedList<>();

            File file = this.manifest.file();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                String line = null;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.equals("") || line.startsWith("//")) {
                        continue;
                    }
                    if (line.startsWith("require")) {
                        String rest = line.substring("require".length()).trim();
                        File sub = new File(file.getParent(), rest);
                        list.add(sub);
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to parse aggregating resource file: " + file, e);
            }
        }

        return filesList;
    }

    @Override
    public void readContent(RequestContext ctx, BinaryContentSink sink) {
        try {
            for (File file: getFilesList()) {
                Buffer buffer = manifest.vertx().fileSystem().readFileSync(file.getPath());
                sink.accept(buffer.getByteBuf());
            }
        } catch (Exception e) {
            log.debug("Failed to serve content: " + ctx.resourcePath(), e);
        } finally {
            sink.close();
        }
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        responder.deleteNotSupported(this);
    }

    private Resource parent;
    private String id;
    private FileResource manifest;
    private List<File> filesList;

    private static final Logger log = Logger.getLogger(AggregatingResource.class);
}
