package io.liveoak.security.policy.acl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigMappingExporter;
import io.liveoak.spi.resource.config.ConfigProperty;
import io.liveoak.spi.resource.config.Configurable;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Configurable
public class AclPolicyRootResource implements RootResource {

    private static final Logger log = Logger.getLogger(AclPolicyRootResource.class);

    private String id;

    private final Map<String, Resource> childResources = new HashMap<>();

    private String configFile;
    private AclPolicyConfig policyConfig;

    public AclPolicyRootResource(String id) {
        this.id = id;
    }

    private void updateConfig(@ConfigProperty("policy-config") String configFile) throws Exception {
        if (configFile == null && configFile != null) {
            log.warn("No policy-config specified");
        } else if (configFile != null && !configFile.equals(this.configFile)) {
            File file = new File(configFile);
            if (file.isFile()) {
                ObjectMapper om = new ObjectMapper();
                this.policyConfig = om.readValue(file, AclPolicyConfig.class);
            } else {
                log.warn(configFile + " not found");
            }
            this.configFile = configFile;
        }
    }

    @ConfigMappingExporter
    public void getConfigFile(HashMap<String, Object> config) {
        config.put("policy-config", configFile);
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        registerChildrenResources(context);
    }

    protected void registerChildrenResources(ResourceContext context) {
        this.childResources.put(AuthzConstants.AUTHZ_CHECK_RESOURCE_ID, new AclPolicyCheckResource(AuthzConstants.AUTHZ_CHECK_RESOURCE_ID, this, context.client()));
    }

    public AclPolicyConfig getPolicyConfig() {
        return policyConfig;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        try {
            if (!this.childResources.containsKey(id)) {
                responder.noSuchResource(id);
                return;
            }

            responder.resourceRead(this.childResources.get(id));

        } catch (Throwable t) {
            responder.internalError(t.getMessage());
        }
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        this.childResources.values().forEach((e) -> {
            sink.accept(e);
        });

        sink.close();
    }
}
