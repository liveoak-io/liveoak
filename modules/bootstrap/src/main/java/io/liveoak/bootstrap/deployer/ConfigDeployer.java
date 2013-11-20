/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.bootstrap.deployer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.bootstrap.Bootstrap;
import io.liveoak.bootstrap.StringPropertyReplacer;
import io.liveoak.container.DefaultContainer;
import io.liveoak.container.SimpleConfig;
import io.liveoak.spi.ConfigurationException;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.resource.RootResource;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

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

        JsonNode tree = mapper.readTree(new File(configFilePath));

        JsonNode resources = tree.get("resources");

        for (Iterator<String> iter = resources.fieldNames(); iter.hasNext(); ) {
            String resourceId = iter.next();
            JsonNode resourceData = resources.get(resourceId);
            String type = resourceData.get("type").asText();

            if (type.equals("jboss-module")) {

                String moduleId = resourceData.get("module-id").asText();
                String className = resourceData.get("class-name").asText();
                JsonNode config = resourceData.get("config");

                SimpleConfig resourceConfig = null;

                if (config != null && config.isObject()) {
                    resourceConfig = new SimpleConfig(mapper.treeToValue(config, Map.class));
                } else {
                    resourceConfig = new SimpleConfig();
                }

                resourceConfig.put("id", resourceId);

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

                    RootResource resource = construct(resourceId, resourceClass);

                    this.container.registerResource(resource, resourceConfig);
                    System.err.println("registered resource: " + resource);
                } catch (Exception e) {
                    System.err.println("Unable to deploy '" + resourceId + "': " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("unknown resource deployment type: " + type);
            }
        }
    }

    protected RootResource construct(String resourceId, Class<? extends RootResource> resourceClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Constructor<? extends RootResource> ctor = null;
        try {
            ctor = resourceClass.getConstructor(String.class);
            return ctor.newInstance(resourceId);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            ctor = resourceClass.getConstructor();
            return ctor.newInstance();
        }
    }


    private DefaultContainer container;
}
