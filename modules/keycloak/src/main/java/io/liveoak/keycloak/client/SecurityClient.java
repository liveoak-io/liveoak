package io.liveoak.keycloak.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import io.liveoak.keycloak.KeycloakConfig;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.exceptions.ResourceProcessingException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.keycloak.representations.idm.ApplicationRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.util.JsonSerialization;

import static io.liveoak.spi.ResourceErrorResponse.ErrorType;

/**
 * Security client for Keycloak.
 *
 * @author Ken Finnigan
 */
public class SecurityClient {

    public SecurityClient(KeycloakConfig config) {
        this.config = config;
    }

    // Application methods

    public List<ApplicationRepresentation> applications(String authToken, String realmName) throws Exception {
        HttpGet get = new HttpGet(realmApplicationsEndpoint(realmName));
        addAuthzHeader(get, authToken);

        return exec(get, 200, "Unable to retrieve application list from " + realmName, entity -> {
            try (InputStream is = entity.getContent()) {
                return JsonSerialization.readValue(is, TypedApplicationList.class);
            } catch (IOException e) {
                throw new ResourceProcessingException(ErrorType.INTERNAL_ERROR,
                        "Unable to retrieve application list from " + realmName, e);
            }
        });
    }

    public ApplicationRepresentation application(String authToken, String realmName, String appName) throws Exception {
        HttpGet get = new HttpGet(applicationEndpoint(realmName, appName));
        addAuthzHeader(get, authToken);

        return exec(get, 200, "Unable to retrieve application " + appName, entity -> {
            try (InputStream is = entity.getContent()) {
                return JsonSerialization.readValue(is, ApplicationRepresentation.class);
            } catch (IOException e) {
                throw new ResourceProcessingException(ErrorType.INTERNAL_ERROR,
                        "Unable to retrieve application " + appName, e);
            }
        });
    }

    public void updateApplication(String authToken, String realmName, ApplicationRepresentation app) throws Exception {
        HttpPut put = new HttpPut(applicationEndpoint(realmName, app.getName()));
        addAuthzHeader(put, authToken);
        put.setEntity(new StringEntity(JsonSerialization.writeValueAsString(app), ContentType.create(MediaType.JSON.toString(), "UTF-8")));

        exec(put, 204, "Unable to update application named " + app.getName() + " in " + realmName + " realm", new VoidHandler());
    }

    public void createApplication(String authToken, String realmName, String appName) throws Exception {
        ApplicationRepresentation app = new ApplicationRepresentation();
        app.setName(appName);

        HttpPost post = new HttpPost(realmApplicationsEndpoint(realmName));
        addAuthzHeader(post, authToken);
        post.setEntity(new StringEntity(JsonSerialization.writeValueAsString(app), ContentType.create(MediaType.JSON.toString(), "UTF-8")));

        exec(post, 201, "Unable to create application named " + appName + " in " + realmName + " realm", new VoidHandler());
    }

    public void deleteApplication(String authToken, String realmName, String appName) throws Exception {
        HttpDelete delete = new HttpDelete(applicationEndpoint(realmName, appName));
        addAuthzHeader(delete, authToken);

        exec(delete, 204, "Unable to delete application named " + appName + " in " + realmName + " realm", new VoidHandler());
    }

    // Application Role methods

    public List<RoleRepresentation> applicationRoles(String authToken, String realmName, String appName) throws Exception {
        HttpGet get = new HttpGet(applicationRolesEndpoint(realmName, appName));
        addAuthzHeader(get, authToken);

        return exec(get, 200, "Unable to retrieve application roles list from " + appName, entity -> {
            try (InputStream is = entity.getContent()) {
                return JsonSerialization.readValue(is, TypedRoleList.class);
            } catch (IOException e) {
                throw new ResourceProcessingException(ErrorType.INTERNAL_ERROR,
                        "Unable to retrieve application roles list from " + appName, e);
            }
        });

    }

    public void createApplicationRole(String authToken, String realmName, String appName, String roleName) throws Exception {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);

        HttpPost post = new HttpPost(applicationRolesEndpoint(realmName, appName));
        addAuthzHeader(post, authToken);
        post.setEntity(new StringEntity(JsonSerialization.writeValueAsString(role), ContentType.create(MediaType.JSON.toString(), "UTF-8")));

        exec(post, 201, "Unable to create application role named " + roleName + " in application " + appName, new VoidHandler());
    }

    public void deleteApplicationRole(String authToken, String realmName, String appName, String roleName) throws Exception {
        HttpDelete delete = new HttpDelete(roleEndpoint(realmName, appName, roleName));
        addAuthzHeader(delete, authToken);

        exec(delete, 204, "Unable to delete application role named " + roleName + " in application " + appName, new VoidHandler());
    }

    // Scope mapping methods

    public MappingsRepresentation clientScopeMappings(String authToken, String realmName, String clientApp) throws Exception {
        HttpGet get = new HttpGet(clientScopeMappingsEndpoint(realmName, clientApp));
        addAuthzHeader(get, authToken);

        return exec(get, 200, "Unable to retrieve scope mappings from " + clientApp, entity -> {
            try (InputStream is = entity.getContent()) {
                return JsonSerialization.readValue(is, MappingsRepresentation.class);
            } catch (IOException e) {
                throw new ResourceProcessingException(ErrorType.INTERNAL_ERROR,
                        "Unable to retrieve scope mappings from " + clientApp, e);
            }
        });
    }

    public List<RoleRepresentation> clientScopeRolesForApplication(String authToken, String realmName, String clientApp, String appName) throws Exception {
        HttpGet get = new HttpGet(clientScopeForApplicationsEndpoint(realmName, clientApp, appName));
        addAuthzHeader(get, authToken);

        return exec(get, 200, "Unable to retrieve roles in client scope for " + clientApp, entity -> {
            try (InputStream is = entity.getContent()) {
                return JsonSerialization.readValue(is, TypedRoleList.class);
            } catch (IOException e) {
                throw new ResourceProcessingException(ErrorType.INTERNAL_ERROR,
                        "Unable to retrieve roles in client scope for " + clientApp, e);
            }
        });
    }

    public void addClientScopeRolesForApplication(String authToken, String realmName, String clientApp, String appName, List<RoleRepresentation> roles) throws Exception {
        HttpPost post = new HttpPost(clientScopeForApplicationsEndpoint(realmName, clientApp, appName));
        addAuthzHeader(post, authToken);
        post.setEntity(new StringEntity(JsonSerialization.writeValueAsString(roles), ContentType.create(MediaType.JSON.toString(), "UTF-8")));

        exec(post, 204, "Unable to add roles to client application " + clientApp, new VoidHandler());
    }

    public void removeClientScopeRolesForApplication(String authToken, String realmName, String clientApp, String appName, List<RoleRepresentation> roles) throws Exception {
        HttpDeleteWithEntity delete = new HttpDeleteWithEntity(clientScopeForApplicationsEndpoint(realmName, clientApp, appName));
        addAuthzHeader(delete, authToken);
        delete.setEntity(new StringEntity(JsonSerialization.writeValueAsString(roles), ContentType.create(MediaType.JSON.toString(), "UTF-8")));

        exec(delete, 204, "Unable to delete roles on client application " + clientApp, new VoidHandler());
    }

    public List<RoleRepresentation> availableClientScopeRolesForApplication(String authToken, String realmName, String clientApp, String appName) throws Exception {
        HttpGet get = new HttpGet(availableClientScopesForApplicationsEndpoint(realmName, clientApp, appName));
        addAuthzHeader(get, authToken);

        return exec(get, 200, "Unable to retrieve available client scopes for client " + clientApp, entity -> {
            try (InputStream is = entity.getContent()) {
                return JsonSerialization.readValue(is, TypedRoleList.class);
            } catch (IOException e) {
                throw new ResourceProcessingException(ErrorType.INTERNAL_ERROR,
                        "Unable to retrieve available client scopes for client " + clientApp, e);
            }
        });
    }


    // Helper methods

    private <T> T exec(HttpRequestBase request, int expectedResponse, String failureMessage, ExecReturn<HttpEntity, T> responseHandler) throws Exception {
        try (CloseableHttpClient client = httpClient()) {
            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getStatusLine().getStatusCode() != expectedResponse) {
                    throw new ResourceProcessingException(ErrorType.INTERNAL_ERROR,
                            failureMessage + ", due to: " + response.getStatusLine().getReasonPhrase());
                }
                return responseHandler.apply(response.getEntity());
            } catch (IOException e) {
                throw new ResourceProcessingException(ErrorType.INTERNAL_ERROR, failureMessage, e);
            }
        }
    }

    private void addAuthzHeader(HttpRequestBase request, String authToken) {
        request.addHeader("Authorization", "Bearer " + authToken);
    }

    private String realmEndpoint(String realmName) {
        return this.config.getBaseUrl() + "/realms/" + realmName;
    }

    private String realmApplicationsEndpoint(String realmName) {
        return realmEndpoint(realmName) + "/applications";
    }

    private String applicationEndpoint(String realmName, String appName) {
        return realmApplicationsEndpoint(realmName) + "/" + appName;
    }

    private String applicationRolesEndpoint(String realmName, String appName) {
        return applicationEndpoint(realmName, appName) + "/roles";
    }

    private String roleEndpoint(String realmName, String appName, String roleName) {
        return applicationRolesEndpoint(realmName, appName) + "/" + roleName;
    }

    private String clientScopeMappingsEndpoint(String realmName, String clientApp) {
        return applicationEndpoint(realmName, clientApp) + "/scope-mappings";
    }

    private String clientScopeForApplicationsEndpoint(String realmName, String clientApp, String appName) {
        return clientScopeMappingsEndpoint(realmName, clientApp) + "/applications/" + appName;
    }

    private String availableClientScopesForApplicationsEndpoint(String realmName, String clientApp, String appName) {
        return clientScopeForApplicationsEndpoint(realmName, clientApp, appName) + "/available";
    }

    private CloseableHttpClient httpClient() {
        return HttpClients.custom()
                .disableContentCompression()
                .build();
    }

    private KeycloakConfig config;

    @FunctionalInterface
    interface ExecReturn<T, R> {
        R apply(T t) throws Exception;
    }

    class VoidHandler implements ExecReturn<HttpEntity, Void> {
        @Override
        public Void apply(HttpEntity entity) {
            return null;
        }
    }

    class HttpDeleteWithEntity extends HttpEntityEnclosingRequestBase {
        public HttpDeleteWithEntity(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return "DELETE";
        }
    }

    static class TypedApplicationList extends ArrayList<ApplicationRepresentation> {
    }

    static class TypedRoleList extends ArrayList<RoleRepresentation> {
    }
}
