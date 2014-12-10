package io.liveoak.scripts.resource;

import java.io.File;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.spi.util.ObjectMapperFactory;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import org.jboss.logging.Logger;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public abstract class ScriptsResource implements Resource {

    protected abstract File getScriptsDirectory();
    protected abstract ScriptResource generateChildResource(ResourceState state) throws Exception;

    private static final String METADATA_FILENAME = "metadata.json";
    private static final String SOURCE_FILENAME = "source.js";

    protected ScriptsRootResource parent;
    protected Vertx vertx;

    public ScriptsResource(Vertx vertx) {
        this.vertx = vertx;
    }

    public void parent(ScriptsRootResource parent) {
        this.parent = parent;
    }

    public Resource parent() {
        return parent;
    }

    public Logger logger() {
        return parent.logger();
    }

    // Read the configuration files from the filesystem [if any] and configure the resource based on them
    public void start() throws Exception {
        File resourceDirectory = getScriptsDirectory();

        vertx.fileSystem().readDir(resourceDirectory.getPath(), (result) -> {
            if (!result.failed()) {

                for (String fileName : result.result()) {
                    File scriptDirectory = new File(fileName);
                    String resourceID = scriptDirectory.getName();

                    try {
                        // read the file containing the metadata for this script
                        File metadataFile = new File(scriptDirectory.getPath() + File.separator + "/" + METADATA_FILENAME);
                        if (metadataFile.exists()) {
                            ObjectNode objectNode = (ObjectNode) ObjectMapperFactory.create().readTree(metadataFile);

                            ResourceState state = ConversionUtils.convert(objectNode);
                            state.id(resourceID);
                            ScriptResource scriptResource = generateChildResource(state);

                            // read the file containing the source for this script [if any]
                            File sourceFile = new File(scriptDirectory.getPath() + File.separator + "/" + SOURCE_FILENAME);
                            if (sourceFile.exists()) {
                                vertx.fileSystem().readFile(sourceFile.getPath(), (buffer) -> {
                                    try {
                                        scriptResource.setScriptBuffer(buffer.result().getByteBuf());
                                    } catch (Exception e) {
                                        //TODO: handle errors better here
                                        e.printStackTrace();
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        logger().error("Error reading resource script files.", e);
                    }
                }

            } else {
                logger().error("Error reading resource script files.", result.cause());
            }
        });
    }

    public void writeMetadataFile(String id, ObjectNode objectNode) throws Exception {
        File scriptDirectory = new File(getScriptsDirectory().getPath() + File.separator + id);
        if (!scriptDirectory.exists()) {
            scriptDirectory.mkdir();
        }
        File metadataFile = new File(scriptDirectory.getPath() + File.separator + METADATA_FILENAME);
        ObjectMapperFactory.createWriter().writeValue(metadataFile, objectNode);
    }

    public void deleteMetadataFile(String id) {
        File scriptDirectory = new File(getScriptsDirectory().getPath() + File.separator + id);
        if (scriptDirectory.exists()) {
            File metadataFile = new File(scriptDirectory.getPath() + File.separator + METADATA_FILENAME);
            metadataFile.delete();
        }
    }

    public void writeSourceFile(String id, ByteBuf byteBuf) throws Exception {
        File scriptDirectory = new File(getScriptsDirectory().getPath() + File.separator + id);
        if (!scriptDirectory.exists()) {
            scriptDirectory.mkdir();
        }
        String filename = scriptDirectory.getPath().toString() + File.separator + SOURCE_FILENAME;
        vertx.fileSystem().writeFileSync(filename, new Buffer(byteBuf));
    }

    public void deleteSourceFile(String id) {
        File scriptDirectory = new File(getScriptsDirectory().getPath() + File.separator + id);
        if (scriptDirectory.exists()) {
            File sourceFile = new File(scriptDirectory.getPath() + File.separator + SOURCE_FILENAME);
            sourceFile.delete();
        }
    }

    public void deleteScriptDirectory(String id) {
        File scriptDirectory = new File(getScriptsDirectory().getPath() + File.separator + id);
        if (scriptDirectory.exists()) {
            scriptDirectory.delete();
        }
    }
}
