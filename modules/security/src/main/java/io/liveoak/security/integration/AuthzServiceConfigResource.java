package io.liveoak.security.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.liveoak.security.spi.AuthzPolicyEntry;
import io.liveoak.security.spi.AuthzServiceConfig;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.config.ConfigResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AuthzServiceConfigResource implements ConfigResource {

    private static final Logger log = Logger.getLogger(AuthzServiceConfigResource.class);

    private AuthzServiceRootResource authzService;

    private String configFile;

    private AuthzServiceConfig config;

    public AuthzServiceConfigResource(AuthzServiceRootResource authzService) {
        this.authzService = authzService;
    }

    @Override
    public Resource parent() {
        return authzService;
    }

    public List<AuthzPolicyEntry> getPolicies() {
        return config != null ? config.getPolicies() : null;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        if (configFile != null) {
            sink.accept("authz-config", configFile);
        }
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        try {
            configFile = (String) state.getProperty("authz-config");
            if (configFile == null) {
                this.config = null;
            } else {
                File file = new File(configFile);
                if (file.isFile()) {
                    ObjectMapper om = new ObjectMapper();
                    config = om.readValue(file, AuthzServiceConfig.class);
                } else {
                    log.warn(configFile + " not found");
                    this.config = null;
                }
            }

            authzService.setPolicies(getPolicies());

            responder.resourceUpdated(this);
        } catch (Throwable t) {
            log.error("", t);
            responder.internalError(t);
        }
    }

}
