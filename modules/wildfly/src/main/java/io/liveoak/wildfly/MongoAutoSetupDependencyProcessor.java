package io.liveoak.wildfly;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import io.liveoak.spi.LiveOak;
import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.SetupAction;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;

import static org.jboss.as.ee.component.Attachments.WEB_SETUP_ACTIONS;

/**
 * @author Bob McWhirter
 */
public class MongoAutoSetupDependencyProcessor implements DeploymentUnitProcessor {

    private static final Logger log = Logger.getLogger(MongoAutoSetupDependencyProcessor.class);

    public static final int PRIORITY = 1;
    public static final Phase PHASE = Phase.DEPENDENCIES;

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        if (!DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit)) {
            return; // Skip non web deployments
        }
        log.info("adding dependency on mongo to: " + deploymentUnit.getServiceName());
        deploymentUnit.addToAttachmentList(WEB_SETUP_ACTIONS, new DependencySetupAction());
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    private static class DependencySetupAction implements SetupAction {

        @Override
        public void setup(Map<String, Object> properties) {
            // nothing
        }

        @Override
        public void teardown(Map<String, Object> properties) {
            // nothing
        }

        @Override
        public int priority() {
            return 0;
        }

        @Override
        public Set<ServiceName> dependencies() {
            ServiceName name = LiveOak.LIVEOAK.append("mongo-launcher");
            return Collections.singleton(name);
        }
    }
}
