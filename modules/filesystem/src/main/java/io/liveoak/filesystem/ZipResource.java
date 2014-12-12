package io.liveoak.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.BinaryContentSink;
import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.LazyResourceState;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ZipResource implements BinaryResource {

    FileSystemAdminResource parent;
    String id;

    protected static final Logger log = Logger.getLogger("io.liveoak.filesystem");

    // Property to specify if we should overwrite existing files with files contained within the zip
    public static final String OVERWRITE = "overwrite";

    // Property to specify if we should delete the old directory completely before copying over the new files
    public static final String CLEAN = "clean";

    public ZipResource(FileSystemAdminResource parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    @Override
    public MediaType mediaType() {
        return MediaType.ZIP;
    }

    @Override
    public long contentLength() {
        return 0;
    }

    @Override
    public void readContent(RequestContext ctx, BinaryContentSink sink) throws Exception {
        //We are uploading a zip to the server, there is nothing to be sent back to the client.
        sink.close();
    }

    @Override
    public void updateContent(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

        if (state instanceof LazyResourceState) {
            LazyResourceState lazyState = (LazyResourceState)state;

            // Check if the 'content-type' header is what we are expecting, otherwise throw an error
            if (!lazyState.getContentType().equals(MediaType.ZIP)) {
                responder.invalidRequest("Invalid Request. Only zip files are acceptable");
                return;
            }

            // set to overwrite if the 'overwrite' query parameter is set to true
            String overwriteProperty = ctx.resourceParams().value(OVERWRITE);
            boolean overwrite = overwriteProperty != null && overwriteProperty.equals(Boolean.TRUE.toString());

            String cleanProperty = ctx.resourceParams().value(CLEAN);
            if (cleanProperty != null && cleanProperty.equals(Boolean.TRUE.toString())) {
                if (parent.directory().exists()) {
                    File file = parent.directory();
                    log.info("The 'clean' parameter was set on zip upload. Deleting the existing directory '" + parent.directory().getCanonicalPath() + "'");
                    parent.vertx().fileSystem().deleteSync(file.getCanonicalPath(), true);

                }
            }

            // if the directory specified in the configuration does not currently exist, then create it
            if (!parent.directory().exists()) {
                parent.directory().mkdirs();
            }

            ZipInputStream zipInputStream = new ZipInputStream(lazyState.contentAsStream());
            try {
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {

                    // get the name of the new file to be created
                    File file = new File(parent.directory() + File.separator + zipEntry.getName());
                    if (zipEntry.isDirectory()) {
                        // if the file isn't already a directory and overwrite is set, then delete the old file
                        if (!file.isDirectory() && overwrite) {
                            file.delete();
                        }
                        file.mkdirs();
                    } else {
                        // check if the parent directory exists or not
                        // if not, then create the whole directory tree up to this point
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }

                        //Only copy the file if the file doesn't already exist, or if the query parameter overwrite = true
                        if ((!file.exists() && file.getParentFile().isDirectory()) || overwrite) {
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            try {
                                int zipContent = zipInputStream.read();
                                while (zipContent > 0) {
                                    fileOutputStream.write(zipContent);
                                    zipContent = zipInputStream.read();
                                }
                            } finally {
                                // make sure we close the stream even if there are any errors
                                fileOutputStream.close();
                            }
                        }
                    }
                }
            } finally {
                // make sure we close the stream even if there are any errors
                zipInputStream.close();
            }
            responder.resourceCreated(this);
        } else {
            //We only handle receiving from the a LazyResourceState
            //TODO: also handle if the state is a DefaultBinaryResourceState?
            responder.invalidRequest("Invalid Request");
            log.error("Expected a resource state of type LazyResourceState. Received " + state.getClass());
        }
    }

    @Override
    public boolean willProcessUpdate(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        return true;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }
}
