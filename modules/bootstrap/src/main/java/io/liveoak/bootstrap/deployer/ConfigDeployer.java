package io.liveoak.bootstrap.deployer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import io.liveoak.bootstrap.Bootstrap;
import io.liveoak.bootstrap.StringPropertyReplacer;
import io.liveoak.container.DefaultContainer;
import io.liveoak.container.SimpleConfig;
import io.liveoak.spi.ConfigurationException;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.resource.RootResource;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

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

                        SimpleConfig resourceConfig = null;
                        if (configObj instanceof Map) {
                            Map<String, Object> config = (Map<String, Object>) configObj;
                            resourceConfig = new SimpleConfig(config);
                        } else {
                            resourceConfig = new SimpleConfig();
                        }
                        resourceConfig.put("id", id);

                        Set<String> names = resourceConfig.names();

                        for (String name : names) {
                            try {
                                String configValue = resourceConfig.get(name, null);
                                if (configValue != null) {
                                    configValue = StringPropertyReplacer.replaceProperties(configValue, System.getProperties());
                                    resourceConfig.put(name, configValue);
                                }
                            } catch (ConfigurationException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                        }


                        ModuleLoader loader = ModuleLoader.forClass(Bootstrap.class);
                        try {
                            Module module = loader.loadModule(ModuleIdentifier.create(moduleId));
                            Class<? extends RootResource> resourceClass = (Class<? extends RootResource>) module.getClassLoader().loadClass(className);

                            RootResource resource = resourceClass.newInstance();

                            this.container.registerResource(resource, resourceConfig);
                            System.err.println("registered resource: " + resource);
                        } catch (ModuleLoadException e) {
                            e.printStackTrace();
                            System.err.println("Unable to deploy '" + id + "': " + e.getMessage());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            System.err.println("Unable to deploy '" + id + "': " + e.getMessage());
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                            System.err.println("Unable to deploy '" + id + "': " + e.getMessage());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            System.err.println("Unable to deploy '" + id + "': " + e.getMessage());
                        } catch (InitializationException e) {
                            e.printStackTrace();
                            System.err.println("Unable to deploy '" + id + "': " + e.getMessage());
                        }
                    } else {
                        System.err.println("unknown resource deployment type: " + type);
                    }
                } else {
                    System.err.println("invalid configuration: " + resourceDataObj);
                }
            }
        }
    }


    private DefaultContainer container;
}
