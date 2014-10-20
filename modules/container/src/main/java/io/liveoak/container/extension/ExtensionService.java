package io.liveoak.container.extension;

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.tenancy.ModuleResourceRegistry;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.MountPointResource;
import io.liveoak.spi.resource.RootResource;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class ExtensionService implements Service<Extension> {

    public ExtensionService(String id, Extension extension, ObjectNode fullConfig) {
        this.id = id;
        this.extension = extension;
        this.fullConfig = fullConfig;
        this.common = false;
        if (fullConfig.has("common")) {
            this.common = fullConfig.get("common").asBoolean();
        }
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();

        ServiceName name = Services.extension(this.id);

        ObjectNode extConfig = (ObjectNode) this.fullConfig.get("config");
        if (extConfig == null) {
            extConfig = JsonNodeFactory.instance.objectNode();
        }


        ServiceName moduleName = Services.resource(ZeroExtension.APPLICATION_ID, "system").append("module").append(id);
        target.addService(moduleName, new ValueService<>(new ImmediateValue<>(new ModuleResourceRegistry(this.id, this.extension, target))))
                .install();


        MountService<RootResource> instanceMount = new MountService<>();
        target.addService(moduleName.append("mount"), instanceMount)
                .addDependency(Services.resource(ZeroExtension.APPLICATION_ID, "system"), MountPointResource.class, instanceMount.mountPointInjector())
                .addDependency(moduleName, RootResource.class, instanceMount.resourceInjector())
                .install();

        SystemExtensionContext moduleContext = new SystemExtensionContextImpl(target, this.id, "module", moduleName, extConfig);

        try {
            this.extension.extend(moduleContext);
        } catch (Exception e) {
            throw new StartException(e);
        }

        try {
            ObjectNode extInstanceConfig = (ObjectNode) this.fullConfig.get("instances");
            if (extInstanceConfig != null) {
                //Create the system-instances/module resource so that we can put in there each individual instance

                Iterator<Map.Entry<String, JsonNode>> fieldIterator = extInstanceConfig.fields();
                while (fieldIterator.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fieldIterator.next();
                    SystemExtensionContext extInstanceContext = new SystemExtensionContextImpl(target, this.id, entry.getKey(), moduleName, (ObjectNode) entry.getValue());
                    this.extension.instance(entry.getKey(), extInstanceContext);
                }
            }
        } catch (Exception e) {
            throw new StartException("Error trying to install an instance for '" + this.id + "'.",e);
        }

        log.debug("** Extension activated: " + this.id);
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Extension getValue() throws IllegalStateException, IllegalArgumentException {
        return this.extension;
    }

    String id() {
        return this.id;
    }

    boolean common() {
        return this.common;
    }

    ObjectNode applicationConfiguration() {
        ObjectNode appConfig = (ObjectNode) this.fullConfig.get("app-config");
        if (appConfig != null) {
            return appConfig;
        }

        return JsonNodeFactory.instance.objectNode();
    }


    private final String id;
    private final Extension extension;
    private final ObjectNode fullConfig;
    private boolean common;

    private static final Logger log = Logger.getLogger(ExtensionService.class);

}
