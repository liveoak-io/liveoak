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

/** Deployer for resources defined in a JSON configuration file.
 *
 * <p>The root of the configuration file should be an object which contains
 * a <code>resources</code> key, which itself is an object that contains a
 * key for each deployed resource, which acts as that resource's ID.</p>
 *
 * <p>The value of each resource should be an object that includes, at the
 * minimum, the <code>type</code>, which at this point must be <code>jboss-module</code>.</p>
 *
 * <p>For <code>jboss-module</code> deployments, another key, <code>module-id</code>
 * denotes which module to load, and <code>class-name</code> describes the class
 * to instantiate.</p>
 *
 * <p>Every deployment may have a <code>config</code> key specifying an object
 * to be used as configuration in the resource's <code>initialize()</code> method.
 * The deployer will add the resource's own ID into the configuration under the key
 * of <code>id</code>.</p>
 *
 * <p>When instantiating the resource from a class, it first attempts to use
 * a one-argument constructor which should take a single <code>String></code>
 * parameter, in which the resource's ID will be passed</p>.
 *
 * <p>If the resource lacks a one-arg constructor, a no-arg constructor will
 * be attempted, in which case, the resource should configure its own ID
 * from the configuration provided to its <code>initialize()</code> method.</p>
 *
 * <p>The general format of the file is:</p>
 *
 * <pre>
 * {
 *     resources: {
 *          some_resource_id: {
 *                  type: 'jboss-modules',
 *                  module-id: 'org.whatever.myresource',
 *                  class-name: 'org.whatever.myresource.MyResource',
 *                  config: {
 *                      some_key: some_value,
 *                      some_key: some_value,
 *                      some_key: some_value,
 *                  }
 *          }
 *          some_other_resource_id: {
 *              ...
 *          }
 *     }
 * }
 *
 * </pre>
 *
 * @author Bob McWhirter
 */
public class ConfigDeployer {

    /** Construct a deployer
     *
     * @param container The container to deploy into.
     */
    public ConfigDeployer(DefaultContainer container) {
        this.container = container;
    }

    /** Deploy resources from a JSON configuration file.
     *
     * @param configFilePath The file to read and deploy from.
     * @throws IOException If an error occurs while attempting to read the deployment file.
     */
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
                    System.err.println("registered resource: " + resource.uri() );
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
