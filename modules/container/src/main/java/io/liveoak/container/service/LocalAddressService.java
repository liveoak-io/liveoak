package io.liveoak.container.service;

import io.netty.channel.local.LocalAddress;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class LocalAddressService implements Service<LocalAddress> {

    LocalAddress address = new LocalAddress("liveoak");

    @Override
    public void start(StartContext context) throws StartException {
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public LocalAddress getValue() throws IllegalStateException, IllegalArgumentException {
        return address;
    }

}
