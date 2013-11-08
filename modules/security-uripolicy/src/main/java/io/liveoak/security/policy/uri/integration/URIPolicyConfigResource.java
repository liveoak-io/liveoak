package io.liveoak.security.policy.uri.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.liveoak.container.auth.SimpleLogger;
import io.liveoak.security.policy.uri.complex.URIPolicy;
import io.liveoak.security.policy.uri.complex.URIPolicyRule;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.config.ConfigResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.io.File;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class URIPolicyConfigResource implements ConfigResource {

    private static final SimpleLogger log = new SimpleLogger(URIPolicyConfigResource.class);

    private URIPolicyRootResource uriPolicy;

    private String config;

    public URIPolicyConfigResource(URIPolicyRootResource authzService) {
        this.uriPolicy = authzService;
    }

    @Override
    public Resource parent() {
        return uriPolicy;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        if (config != null) {
            sink.accept("policy-config", config);
        }
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        try {
            String config = (String) state.getProperty("policy-config");
            if (config == null && this.config != null) {
                log.info("No config specified");
                uriPolicy.setUriPolicy(null);
                this.config = null;
            } else if (config != null && !config.equals(this.config)) {
                File file = new File(config);
                if (file.isFile()) {
                    ObjectMapper om = new ObjectMapper();
                    URIPolicyConfig c = om.readValue(file, URIPolicyConfig.class);
                    URIPolicy policy = createPolicy(c);
                    uriPolicy.setUriPolicy(policy);
                } else {
                    // TODO LOG
                    System.out.println(config + " not found");
                }
                this.config = config;
            }
            responder.resourceUpdated(this);
        } catch (Throwable t) {
            t.printStackTrace();
            responder.internalError(t);
        }
    }

    protected URIPolicy createPolicy(URIPolicyConfig uriPolicyConfig) {
        return new URIPolicy((policy) -> {

            for (URIPolicyConfigRule cfgRule : uriPolicyConfig.getUriRules()) {
                URIPolicyRule internalRule = URIPolicyRule.createRule(cfgRule.getPriority(), cfgRule.getUriPattern(),
                        cfgRule.getQueryParamsCondition(), cfgRule.getRequestType(), cfgRule.getAllowedRoles(), cfgRule.getDeniedRoles(),
                        cfgRule.getAllowedUsers(), cfgRule.getDeniedUsers());
                policy.addURIPolicyRule(internalRule);
            }

        });
    }

}
