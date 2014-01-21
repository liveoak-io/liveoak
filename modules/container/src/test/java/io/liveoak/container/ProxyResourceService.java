package io.liveoak.container;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ProxyResourceService implements Service<RootResource> {

    public ProxyResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        boolean blocking = false;
        ObjectNode config = this.configurationInjector.getValue();
        if ( config.has( "blocking" ) ) {
            System.err.println( "-" + config.get( "blocking" ) );
            blocking = config.get( "blocking" ).asBoolean();
        }

        System.err.println( "start: " + blocking );

        if ( blocking ) {
            this.resource = new BlockingProxyResource( this.id, this.clientInjector.getValue()  );
        } else {
            this.resource = new NonBlockingProxyResource( this.id, this.clientInjector.getValue() );
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public RootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<ObjectNode> configurationInjector() {
        return this.configurationInjector;
    }

    public Injector<Client> clientInjector() {
        return this.clientInjector;
    }

    private String id;

    private InjectedValue<ObjectNode> configurationInjector = new InjectedValue<>();
    private InjectedValue<Client> clientInjector = new InjectedValue<>();

    private RootResource resource;
}
