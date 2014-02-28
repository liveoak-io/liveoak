package org.liveoak.testsuite.internal.server;

import java.util.Map;

/**
 * LiveOak server embedded in same JVM like test. Useful for debugging
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class EmbeddedLiveOakServer extends LiveOakServer {

    @Override
    protected void startImpl() throws Throwable {
        long time = System.currentTimeMillis();

        for (Map.Entry<String, String> property : jvmProperties.entrySet()) {
            System.setProperty(property.getKey(), property.getValue());
        }

        org.jboss.modules.Main.main(jvmArguments);

        // waitFor("http://localhost:8080", Config.startTimeout());
        log.info("Started embedded LiveOakServer in " + (System.currentTimeMillis() - time) + " ms");
    }

    @Override
    public void stop() throws Exception {
        // For now, rely on MSC shutdown hooks to stop it
    }
}
