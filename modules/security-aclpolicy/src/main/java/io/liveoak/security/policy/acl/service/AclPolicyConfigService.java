package io.liveoak.security.policy.acl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.security.policy.acl.AclPolicyConfig;
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
public class AclPolicyConfigService implements Service<AclPolicyConfig> {
    @Override
    public void start(StartContext context) throws StartException {
        File file = this.fileInjector.getValue();

        if (file.exists()) {
            try {
                ObjectMapper om = ObjectMapperFactory.create();
                this.policy = om.readValue(file, AclPolicyConfig.class);
            } catch (IOException e) {
                throw new StartException( e );
            }
        } else {
            this.policy = new AclPolicyConfig();
        }
    }

    @Override
    public void stop(StopContext context) {
        this.policy = null;
    }

    @Override
    public AclPolicyConfig getValue() throws IllegalStateException, IllegalArgumentException {
        return this.policy;
    }

    public Injector<File> fileInjector() {
        return this.fileInjector;
    }

    private InjectedValue<File> fileInjector = new InjectedValue<>();
    private AclPolicyConfig policy;
}
