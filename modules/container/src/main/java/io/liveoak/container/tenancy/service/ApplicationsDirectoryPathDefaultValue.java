package io.liveoak.container.tenancy.service;

import java.io.IOException;
import java.nio.file.Files;

import org.jboss.msc.value.Value;

/**
 * To be used as a default value when creating a service to specify where the applications directory exists.
 * An example usage is:
 * <code>
 *     serviceTarget.addService(LIVEOAK.append("apps-dir"),
 *         new ValueService<>(new DefaultValue<>(new ImmediateValue<>(this.appsDir != null ? this.appsDir.getAbsolutePath() : null), new ApplicationsDirectoryPathDefaultValue())))
 *         .install();
 * </code>
 *
 * @author Ken Finnigan
 */
public class ApplicationsDirectoryPathDefaultValue implements Value<String> {
    @Override
    public String getValue() throws IllegalStateException, IllegalArgumentException {
        try {
            return Files.createTempDirectory("liveoak").toFile().getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
