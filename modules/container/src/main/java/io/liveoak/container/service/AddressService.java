package io.liveoak.container.service;

import io.liveoak.spi.container.Address;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AddressService implements Address, Service<Address> {
    private String host;
    private int port;
    private int portUndertow;

    public AddressService(String host, int port, int portUndertow) {
        this.host = host;
        this.port = port;
        this.portUndertow = portUndertow;
    }

    @Override
    public void start(StartContext context) throws StartException {
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Address getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void host(String host) {
        this.host = host;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public void port(int port) {
        this.port = port;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public void portUndertow(int portUndertow) {
        this.portUndertow = portUndertow;
    }

    @Override
    public int portUndertow() {
        return portUndertow;
    }
}
