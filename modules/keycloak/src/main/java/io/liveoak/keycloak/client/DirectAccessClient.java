package io.liveoak.keycloak.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.liveoak.keycloak.KeycloakConfig;
import io.liveoak.spi.LiveOak;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.ServiceUrlConstants;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.KeycloakUriBuilder;

/**
 * @author Ken Finnigan
 */
public class DirectAccessClient {
    public DirectAccessClient(KeycloakConfig config) {
        this.config = config;
    }

    public String accessToken() throws IOException {
        if (openConnections.get() == 0 && accessTokenResponse == null) {

            synchronized (this) {
                if (accessTokenResponse == null) {
                    // Not connected
                    int attempts = 0;
                    boolean intr = false;

                    try {
                        while (accessTokenResponse == null && attempts < MAX_RETRIES) {

                            try (CloseableHttpClient client = HttpClientBuilder.create().build()) {

                                HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(config.getBaseUrl())
                                        .path(ServiceUrlConstants.TOKEN_SERVICE_DIRECT_GRANT_PATH).build(LiveOak.LIVEOAK_APP_REALM));

                                List<NameValuePair> formparams = new ArrayList<>();
                                formparams.add(new BasicNameValuePair("username", "liveoak-server"));

                                String initialPassword = System.getProperty("liveoak.initial.password");
                                if (initialPassword != null) {
                                    formparams.add(new BasicNameValuePair("password", initialPassword));
                                } else {
                                    formparams.add(new BasicNameValuePair("password", "password"));
                                }

                                formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, "liveoak-admin-client"));
                                UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
                                post.setEntity(form);

                                HttpResponse response = client.execute(post);
                                int status = response.getStatusLine().getStatusCode();
                                HttpEntity entity = response.getEntity();

                                if (status == 404) {
                                    attempts++;
                                    wait(TIMEOUT);
                                    continue;
                                } else if (status != 200) {
                                    String json = EntityUtils.toString(entity);
                                    throw new IOException("Bad status: " + status + ", response: " + json);
                                }

                                if (entity == null) {
                                    throw new IOException("No Entity");
                                }

                                accessTokenResponse = JsonSerialization.readValue(EntityUtils.toString(entity), AccessTokenResponse.class);
                            } catch (InterruptedException e) {
                                intr = true;
                            }
                        }
                    } finally {
                        if (intr) Thread.currentThread().interrupt();
                    }
                }
            }
        }

        openConnections.incrementAndGet();
        return accessTokenResponse.getToken();
    }

    public void close() {
        if (openConnections.decrementAndGet() == 0) {
            logout(accessTokenResponse);
        }
    }

    public void shutdown() {
        if (openConnections.get() > 0 && accessTokenResponse != null) {
            logout(accessTokenResponse);
        }
    }

    private void logout(AccessTokenResponse accessTokenResponse) {
        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(config.getBaseUrl())
                    .path(ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH).build("liveoak-apps"));

            List<NameValuePair> formparams = new ArrayList<>();
            formparams.add(new BasicNameValuePair(OAuth2Constants.REFRESH_TOKEN, accessTokenResponse.getRefreshToken()));
            formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, "liveoak-admin-client"));
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
            post.setEntity(form);

            client.execute(post);
        } catch (IOException e) {
            // Not a problem we need to report
        }

        this.accessTokenResponse = null;
    }

    private volatile Object lock = new Object();
    private AccessTokenResponse accessTokenResponse;
    private AtomicInteger openConnections = new AtomicInteger(0);
    private KeycloakConfig config;

    //TODO Make configurable?
    private static final int MAX_RETRIES = 3;
    private static final long TIMEOUT = 3 * 1000;
}
