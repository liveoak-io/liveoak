package io.liveoak.scripts.resourcetriggered.resource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.scripts.resource.ScriptsRootResource;
import io.liveoak.scripts.resourcetriggered.manager.ResourceScriptManager;
import io.liveoak.spi.InvalidPropertyTypeException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import org.jboss.logging.Logger;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ResourceScripts implements Resource {

    private ScriptsRootResource parent;
    private static final String ID = "resource-triggered-scripts";

    private static final String RESOURCE_DIRNAME = "resource-based";
    private static final String METADATA_FILENAME = "metadata.json";
    private static final String SOURCE_FILENAME = "source.js";

    private ResourceScriptManager resourceInterceptorManager;
    private Vertx vertx;

    private Map<String, ResourceScript> scripts;

    File resourceDirectory;

    public ResourceScripts(ResourceScriptManager resourceScriptManager, Vertx vertx) {
        this.scripts = new HashMap<>();
        this.resourceInterceptorManager = resourceScriptManager;
        this.vertx = vertx;
    }

    public void parent(ScriptsRootResource parent) {
        this.parent = parent;
    }

    // Read the configuration files from the filesystem [if any] and configure the resource based on them
    public void start() throws IOException {

        //get a reference to where the resource triggred scripts should be held
        String resourceBasedDir = parent.getScriptDirectory() + File.separator + "/" + RESOURCE_DIRNAME;
        resourceDirectory = new File(resourceBasedDir);

        // create the directory if it doesn't already exist
        if (!resourceDirectory.exists()) {
            resourceDirectory.mkdirs();
        }

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

                            ResourceScript resourceScript = ResourceScript.generate(this, resourceID, state);
                            // add this resource to the list of scripts available.
                            this.scripts.put(resourceID, resourceScript);

                            // read the file containing the source for this script [if any]
                            File sourceFile = new File(scriptDirectory.getPath() + File.separator + "/" + SOURCE_FILENAME);
                            if (sourceFile.exists()) {
                                vertx.fileSystem().readFile(sourceFile.getPath(), (buffer) -> {
                                    resourceScript.setScriptFile(buffer.result().getByteBuf());
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

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("name", "Resource Triggered Scripts");
        sink.accept("description", "Scripts which are run when a monitored resource is modified.");
        sink.accept("count", scripts.size());
        sink.close();
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        try {
            ResourceScript resourceScript = ResourceScript.generate(this, state);

            scripts.put(resourceScript.id(), resourceScript);
            //resourceInterceptorManager.addResource(resourceScript);

            //Write to the file system
            ObjectNode objectNode = ConversionUtils.convert(state);
            writeMetadataFile(resourceScript.id(), objectNode);

            responder.resourceCreated(resourceScript);
        } catch (InvalidPropertyTypeException e) {
            responder.invalidRequest(e.getMessage());
        }
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        for (ResourceScript scriptResource : scripts.values()) {
            sink.accept(scriptResource);
        }
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        ResourceScript script = scripts.get(id);
        if (script != null) {
            responder.resourceRead(script);
        } else {
            responder.noSuchResource(id);
        }
    }

    public void deleteMember(RequestContext ctx, String id, Responder responder) throws Exception {
        ResourceScript script = scripts.get(id);
        if (script != null) {
            ScriptFileResource scriptFile = script.getScriptFile();
            if (scriptFile != null) {
                getResourceInterceptorManager().removeResource(id);
                deleteSourceFile(id);
            }
            deleteMetadataFile(id);
            deleteScriptDirectory(id);
            scripts.remove(id);
            responder.resourceDeleted(script);
        } else {
            responder.noSuchResource(id);
        }
    }

    ResourceScriptManager getResourceInterceptorManager() {
        return this.resourceInterceptorManager;
    }


    public Logger logger() {
        return parent.logger();
    }

    public void writeMetadataFile(String id, ObjectNode objectNode) throws Exception {
        File scriptDirectory = new File(resourceDirectory.getPath() + File.separator + id);
        if (!scriptDirectory.exists()) {
            scriptDirectory.mkdir();
        }
        File metadataFile = new File(scriptDirectory.getPath() + File.separator + METADATA_FILENAME);
        ObjectMapperFactory.createWriter().writeValue(metadataFile, objectNode);
    }

    public void deleteMetadataFile(String id) {
        File scriptDirectory = new File(resourceDirectory.getPath() + File.separator + id);
        if (scriptDirectory.exists()) {
            File metadataFile = new File(scriptDirectory.getPath() + File.separator + METADATA_FILENAME);
            metadataFile.delete();
        }
    }

    public void writeSourceFile(String id, ByteBuf byteBuf) throws Exception {
        File scriptDirectory = new File(resourceDirectory.getPath() + File.separator + id);
        if (!scriptDirectory.exists()) {
            scriptDirectory.mkdir();
        }
        String filename = scriptDirectory.getPath().toString() + File.separator + SOURCE_FILENAME;
        vertx.fileSystem().writeFileSync(filename, new Buffer(byteBuf));
    }

    public void deleteSourceFile(String id) {
        File scriptDirectory = new File(resourceDirectory.getPath() + File.separator + id);
        if (scriptDirectory.exists()) {
            File sourceFile = new File(scriptDirectory.getPath() + File.separator + SOURCE_FILENAME);
            sourceFile.delete();
        }
    }

    public void deleteScriptDirectory(String id) {
        File scriptDirectory = new File(resourceDirectory.getPath() + File.separator + id);
        if (scriptDirectory.exists()) {
            scriptDirectory.delete();
        }
    }
}
