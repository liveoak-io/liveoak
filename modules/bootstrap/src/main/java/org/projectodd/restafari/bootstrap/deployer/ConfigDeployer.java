package org.projectodd.restafari.bootstrap.deployer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.projectodd.restafari.bootstrap.Bootstrap;
import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.spi.InitializationException;
import org.projectodd.restafari.spi.resource.RootResource;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public class ConfigDeployer {

    public ConfigDeployer(DefaultContainer container) {
        this.container = container;
    }

    public void deploy(String configFilePath) throws IOException {
        System.err.println("deploying from: " + configFilePath);

        JsonFactory factory = new JsonFactory();
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        ObjectMapper mapper = new ObjectMapper(factory);

        Map<?, ?> value = mapper.readValue(new File(configFilePath), Map.class);

        Object resourcesObj = value.get("resources");

        if (resourcesObj instanceof Map) {
            Map<?, ?> resources = (Map<?, ?>) resourcesObj;

            for (Object keyObj : resources.keySet()) {
                String id = keyObj.toString();
                Object resourceDataObj = resources.get(keyObj);
                if (resourceDataObj instanceof Map) {
                    Map<?, ?> resourceData = (Map<?, ?>) resourceDataObj;

                    String type = resourceData.get("type").toString();

                    if (type.equals("jboss-module")) {
                        String moduleId = resourceData.get("module-id").toString();
                        String className = resourceData.get("class-name").toString();
                        Object configObj = resourceData.get("config");

                        if (configObj instanceof Map) {
                            Map<String, Object> config = (Map<String, Object>) configObj;
                            config.put("id", id);
                            SimpleConfig resourceConfig = new SimpleConfig(config);

                            ModuleLoader loader = ModuleLoader.forClass(Bootstrap.class);
                            try {
                                Module module = loader.loadModule(ModuleIdentifier.create(moduleId));
                                Class<? extends RootResource> resourceClass = (Class<? extends RootResource>) module.getClassLoader().loadClass(className);

                                RootResource resource = resourceClass.newInstance();

                                this.container.registerResource(resource, resourceConfig);
                            } catch (ModuleLoadException e) {
                                System.err.println("Unable to deploy '" + id + "': " + e.getMessage());
                            } catch (ClassNotFoundException e) {
                                System.err.println("Unable to deploy '" + id + "': " + e.getMessage());
                            } catch (InstantiationException e) {
                                System.err.println("Unable to deploy '" + id + "': " + e.getMessage());
                            } catch (IllegalAccessException e) {
                                System.err.println("Unable to deploy '" + id + "': " + e.getMessage());
                            } catch (InitializationException e) {
                                System.err.println("Unable to deploy '" + id + "': " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }


    private DefaultContainer container;
}
