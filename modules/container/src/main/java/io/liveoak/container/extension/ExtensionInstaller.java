package io.liveoak.container.extension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.ServiceLoader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.json.JSONEncoder;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.common.util.StringPropertyReplacer;
import io.liveoak.container.extension.service.ExtensionService;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.Extension;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StabilityMonitor;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class ExtensionInstaller {

    public ExtensionInstaller(ServiceTarget target) {
        this.target = target.subTarget();
    }

    public void load(File extensionDesc) throws Exception {
        loadFile(extensionDesc, null);
    }

    public void loadFile(File extensionDesc, Extension extension) throws Exception {
        ObjectMapper mapper = ObjectMapperFactory.create();
        // replace the properties before trying to read it as a json document
        // eg "number: $someValue" is not a valid json object.
        ObjectNode fullConfig = (ObjectNode) mapper.readTree(replaceProperties(extensionDesc));
        String id = extensionDesc.getName();
        if (id.endsWith(".json")) {
            id = id.substring(0, id.length() - 5);
        }

        if (extension == null) {
            extension = getExtension(fullConfig);
        }

        load(id, extension, fullConfig, new ExtensionConfigurationManager(id, extensionDesc));
    }

    private Extension getExtension(ObjectNode config) throws Exception {
        String moduleId = config.get("module-id").asText();
        ModuleIdentifier moduleIdentifier = ModuleIdentifier.create(moduleId);
        ModuleLoader loader = ModuleLoader.forClass(this.getClass());

        Module module = loader.loadModule(moduleIdentifier);

        ServiceLoader<Extension> extensions = module.loadService(Extension.class);

        for (Extension extension : extensions) {
            return extension;
        }

        throw new Exception("No extension found in module: " + moduleId);
    }

    public void load(String id, Extension extension) throws Exception {
        load(id, extension, JsonNodeFactory.instance.objectNode());
    }

    public void load(Extension extension, File file) throws Exception {
        ObjectMapper mapper = ObjectMapperFactory.create();
        ObjectNode fullConfig = (ObjectNode) mapper.readTree(replaceProperties(file));
        String id = file.getName();
        if (id.endsWith(".json")) {
            id = id.substring(0, id.length() - 5);
        }

        load(id, extension, fullConfig, new ExtensionConfigurationManager(id, file));
    }

    public void load(String id, Extension extension, ObjectNode config) throws Exception {
        load(id, extension, config, new ExtensionConfigurationManager(id, null));
    }

    public void load(String id, Extension extension, ObjectNode config, ExtensionConfigurationManager extensionConfigManager) throws Exception {
        ServiceName configManagerName = Services.systemConfigurationManager(id);

        target.addService(configManagerName, new ValueService<>(new ImmediateValue<>(extensionConfigManager))).install();
        target.addService(Services.systemEnvironmentProperties(id), new ValueService<>(new ImmediateValue<>(envProperties()))).install();

        StabilityMonitor monitor = new StabilityMonitor();
        ServiceTarget target = this.target.subTarget();
        target.addMonitor(monitor);
        ExtensionService extensionService = new ExtensionService(id, extension, config, extensionConfigManager);
        ServiceBuilder builder = target.addService(Services.extension(id), extensionService);

        JsonNode deps = config.get("dependencies");
        if (deps != null) {
            for (JsonNode node : deps) {
                builder.addDependency(Services.extension(node.asText()));
            }
        }
        builder.install();
        monitor.awaitStability();
    }

    private String replaceProperties(File file) throws IOException {
        String original = new String(Files.readAllBytes(file.toPath()));
        return StringPropertyReplacer.replaceProperties(original, System.getProperties(), (v) -> JSONEncoder.jsonStringEscape(v));
    }

    private Properties envProperties() {
        Properties props = new Properties(System.getProperties());
        return props;
    }

    private ServiceTarget target;
}
