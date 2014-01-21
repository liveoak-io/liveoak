package io.liveoak.security.policy.uri.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.liveoak.security.policy.uri.complex.InitializationWorker;
import io.liveoak.security.policy.uri.complex.URIPolicy;
import io.liveoak.security.policy.uri.complex.URIPolicyRule;
import io.liveoak.security.policy.uri.integration.URIPolicyConfig;
import io.liveoak.security.policy.uri.integration.URIPolicyConfigRule;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.io.File;
import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class URIPolicyService implements Service<URIPolicy> {

    @Override
    public void start(StartContext context) throws StartException {
        File file = this.fileInjector.getValue();

        if (file.exists() ) {
            try {
                ObjectMapper om = new ObjectMapper();
                URIPolicyConfig c = om.readValue(file, URIPolicyConfig.class);
                this.policy = createPolicy(c);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.policy = new URIPolicy( new InitializationWorker() {
                @Override
                public void run(URIPolicy uriPolicy) {

                }
            });
        }
    }


    @Override
    public void stop(StopContext context) {
        this.policy = null;
    }

    @Override
    public URIPolicy getValue() throws IllegalStateException, IllegalArgumentException {
        return this.policy;
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

    public Injector<File> fileInjector() {
        return this.fileInjector;
    }

    private InjectedValue<File> fileInjector = new InjectedValue<>();
    private URIPolicy policy;
}
