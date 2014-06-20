package io.liveoak.container.extension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.common.util.StringPropertyReplacer;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.Extension;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StabilityMonitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ServiceLoader;

/**
 * @author Bob McWhirter
 */
public class ExtensionInstaller {


    public ExtensionInstaller(ServiceTarget target, ServiceName systemConfigMount) {
        this.target = target.subTarget();
        this.systemConfigMount = systemConfigMount;
    }

    public void load(File extensionDesc) throws Exception {
        ObjectMapper mapper = ObjectMapperFactory.create();
        // replace the properties before trying to read it as a json document
        // eg "number: $someValue" is not a valid json object.
        ObjectNode fullConfig = (ObjectNode) mapper.readTree(replaceProperties(extensionDesc));
        String id = extensionDesc.getName();
        if (id.endsWith(".json")) {
            id = id.substring(0, id.length() - 5);
        }
        load(id, fullConfig);
    }

    public void load(String id, ObjectNode fullConfig) throws Exception {
        String moduleId = fullConfig.get("module-id").asText();
        load(id, ModuleIdentifier.create(moduleId), fullConfig);
    }

    public void load(String id, ModuleIdentifier moduleId, ObjectNode fullConfig) throws Exception {
        ModuleLoader loader = ModuleLoader.forClass(this.getClass());
        Module module = loader.loadModule(moduleId);

        ServiceLoader<Extension> extensions = module.loadService(Extension.class);

        boolean found = false;

        for (Extension extension : extensions) {
            if (found) {
                throw new Exception("Only one extension allowed per module: " + moduleId );
            }
            load(id, extension, fullConfig );
            found = true;
        }

        if ( ! found ) {
            throw new Exception( "No extension found in module: " + moduleId  );
        }
    }

    public void load(String id, Extension extension) throws Exception {
        load( id, extension, JsonNodeFactory.instance.objectNode() );
    }

    public void load(String id, Extension extension, ObjectNode config) throws Exception {
        StabilityMonitor monitor = new StabilityMonitor();
        ServiceTarget target = this.target.subTarget();
        target.addMonitor( monitor );
        ServiceBuilder builder = target.addService(LiveOak.extension(id), new ExtensionService(id, extension, config));

        JsonNode deps = config.get("dependencies");
        if (deps != null) {
            for (JsonNode node: deps) {
                builder.addDependency(LiveOak.extension(node.asText()));
            }
        }
        builder.install();
        monitor.awaitStability();
    }

    private String replaceProperties(File file) throws IOException{
        String original =  new String(Files.readAllBytes(file.toPath()));
        return StringPropertyReplacer.replaceProperties(original, System.getProperties());
    }

    private ServiceTarget target;
    private final ServiceName systemConfigMount;

}
