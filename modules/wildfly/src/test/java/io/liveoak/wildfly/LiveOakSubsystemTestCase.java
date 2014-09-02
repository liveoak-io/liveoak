package io.liveoak.wildfly;

import java.io.IOException;

import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;

/**
 * @author Tomaz Cerar (c) 2014 Red Hat Inc.
 */
public class LiveOakSubsystemTestCase extends AbstractSubsystemBaseTest {

    public LiveOakSubsystemTestCase() {
        super(LiveOakExtension.SUBSYSTEM_NAME, new LiveOakExtension());
    }

    @Override
    protected String getSubsystemXml() throws IOException {
        return readResource("liveoak-1.0.xml");
    }
}
