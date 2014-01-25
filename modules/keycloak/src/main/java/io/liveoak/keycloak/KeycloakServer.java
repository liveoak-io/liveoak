package io.liveoak.keycloak;

import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.URLResource;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DefaultServletConfig;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.ServletInfo;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;
import org.keycloak.services.filters.KeycloakSessionServletFilter;
import org.keycloak.services.managers.ApplianceBootstrap;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.KeycloakApplication;

import javax.servlet.DispatcherType;
import java.io.IOException;
import java.net.URL;

import static io.undertow.servlet.Servlets.servlet;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakServer {

    private UndertowServer undertow;

    private KeycloakSessionFactory factory;

    public KeycloakServer(UndertowServer undertow) {
        this.undertow = undertow;
    }

    public KeycloakSessionFactory getKeycloakSessionFactory() {
        return factory;
    }

    protected void setupDefaultRealm() {
        KeycloakSession session = factory.createSession();
        session.getTransaction().begin();

        try {
            RealmManager manager = new RealmManager(session);

            if (manager.getRealm(Constants.ADMIN_REALM) != null) {
                return;
            }

            new ApplianceBootstrap().bootstrap(session);

            // No need to require admin to change password as this server is for dev/test
            manager.getRealm(Constants.ADMIN_REALM).getUser("admin").removeRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);

            session.getTransaction().commit();
        } finally {
            session.close();
        }
    }

    public void start() throws Throwable {
        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setApplicationClass(KeycloakApplication.class.getName());

        DeploymentInfo deploymentInfo = new DeploymentInfo();

        deploymentInfo.setClassLoader(getClass().getClassLoader());
        deploymentInfo.setContextPath("/auth");
        deploymentInfo.setDeploymentName("Keycloak");
        deploymentInfo.setResourceManager(new KeycloakResourceManager());

        deploymentInfo.setDefaultServletConfig(new DefaultServletConfig(true));
        deploymentInfo.addWelcomePage("index.html");

        ServletInfo resteasyServlet = servlet("ResteasyServlet", HttpServlet30Dispatcher.class)
                .setAsyncSupported(true)
                .setLoadOnStartup(1)
                .addMapping("/rest/*");
        resteasyServlet.addInitParam("resteasy.servlet.mapping.prefix", "/rest");

        deploymentInfo.addServletContextAttribute(ResteasyDeployment.class.getName(), deployment);
        deploymentInfo.addServlet(resteasyServlet);

        FilterInfo filter = Servlets.filter("SessionFilter", KeycloakSessionServletFilter.class);
        deploymentInfo.addFilter(filter);
        deploymentInfo.addFilterUrlMapping("SessionFilter", "/rest/*", DispatcherType.REQUEST);

        undertow.deploy(deploymentInfo);

        factory = KeycloakApplication.createSessionFactory();

        setupDefaultRealm();
    }

    public void stop() {
        undertow.undeploy("Keycloak");
        factory.close();
    }

    public static class KeycloakResourceManager implements ResourceManager {
        @Override
        public Resource getResource(String path) throws IOException {
            String realPath = "META-INF/resources" + path;
            URL url = getClass().getClassLoader().getResource(realPath);
            return new URLResource(url, url.openConnection(), path);
        }

        @Override
        public boolean isResourceChangeListenerSupported() {
            return false;
        }

        @Override
        public void registerResourceChangeListener(ResourceChangeListener listener) {
        }

        @Override
        public void removeResourceChangeListener(ResourceChangeListener listener) {
        }

        @Override
        public void close() throws IOException {
        }
    }

}
