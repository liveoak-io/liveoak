package io.liveoak.keycloak.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.keycloak.representations.idm.RealmRepresentation;

import java.io.File;
import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class RealmRepresentationService implements Service<RealmRepresentation> {
    public RealmRepresentationService(String orgId, String appId) {
        this.orgId = orgId;
        this.appId = appId;
    }

    @Override
    public void start(StartContext context) throws StartException {
        File file = this.fileInjector.getValue();
        if (file.exists()) {
            try {
                JsonFactory factory = new JsonFactory();
                factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
                factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
                ObjectMapper mapper = new ObjectMapper(factory);
                this.realmRepresentation = mapper.readValue(file, RealmRepresentation.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse json", e);
            }
        } else {
            this.realmRepresentation = new RealmRepresentation();
        }
        this.realmRepresentation.setRealm( this.orgId + "-" + this.appId );
    }

    @Override
    public void stop(StopContext context) {
        this.realmRepresentation = null;
    }

    @Override
    public RealmRepresentation getValue() throws IllegalStateException, IllegalArgumentException {
        return this.realmRepresentation;
    }

    public Injector<File> fileInjector() {
        return this.fileInjector;
    }

    private String orgId;
    private String appId;
    private InjectedValue<File> fileInjector = new InjectedValue<>();

    private RealmRepresentation realmRepresentation;
}
