package io.liveoak.container.tenancy.service;

import io.liveoak.common.util.StringPropertyReplacer;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;

/**
 * Created by mwringe on 20/02/15.
 */
public class ApplicationConfigurationFileService implements Service<Void> {

    private static final Logger log = Logger.getLogger(ApplicationConfigurationFileService.class);

    private final static String CONFIG_SUFFIX = ".config.orig";

    Properties properties;
    List<String> configFiles;
    File directory;

    public ApplicationConfigurationFileService(File directory, List<String> configFiles, Properties properties) {
        this.configFiles = configFiles;
        this.properties = properties;
        this.directory = directory;
    }

    @Override
    public void start(StartContext context) throws StartException {
        try {
            if (configFiles != null && !configFiles.isEmpty()) {
                for (String path : configFiles) {
                    File configFile = new File(this.directory, path);

                    if (configFile.exists()) {

                        File originalConfigFile = new File(configFile.getParent() + "/." + configFile.getName() + CONFIG_SUFFIX);

                        // if the original config files doesn't exist, then move the current value to the backup one
                        if (!originalConfigFile.exists()) {
                            log.info("About to copy " + configFile.toPath() + " to " + originalConfigFile.toPath());
                            Files.copy(configFile.toPath(), originalConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }

                        if (configFile.lastModified() != 0L && configFile.lastModified() <= originalConfigFile.lastModified()) {
                            BufferedReader reader = new BufferedReader(new FileReader(originalConfigFile));
                            BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));

                            String line = reader.readLine();
                            while (line != null) {
                                String newValue = StringPropertyReplacer.replaceProperties(line, properties);
                                writer.write(newValue);
                                writer.write("\n");
                                line = reader.readLine();
                            }

                            reader.close();
                            writer.close();

                            // mark set the last modified time to the current time
                            originalConfigFile.setLastModified(System.currentTimeMillis());
                        }

                    } else {
                        log.warn("No configuration file exists for '" + path + "' in directory " + directory.getAbsolutePath() + ". Skipping");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error trying to handle the application's configuration files", e);
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }



}
