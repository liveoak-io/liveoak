package io.liveoak.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.liveoak.security.spi.AuthzPolicyGroup;
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
public class AuthzPolicyGroupService implements Service<AuthzPolicyGroup> {

    @Override
    public void start(StartContext context) throws StartException {
        File file = this.fileInjector.getValue();
        if (file.exists()) {
            try {
                ObjectMapper om = new ObjectMapper();
                this.group = om.readValue(this.fileInjector.getValue(), AuthzPolicyGroup.class);
            } catch (IOException e) {
                throw new StartException(e);
            }
        } else {
            this.group = new AuthzPolicyGroup();
        }
    }

    @Override
    public void stop(StopContext context) {
        this.group = null;
    }

    @Override
    public AuthzPolicyGroup getValue() throws IllegalStateException, IllegalArgumentException {
        return this.group;
    }

    public Injector<File> fileInjector() {
        return this.fileInjector;
    }

    private InjectedValue<File> fileInjector = new InjectedValue<>();
    private AuthzPolicyGroup group;
}
